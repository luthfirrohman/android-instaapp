package id.app.instaapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import id.app.instaapp.core.AccessControl
import id.app.instaapp.core.Resource
import id.app.instaapp.data.local.PostDao
import id.app.instaapp.data.local.toDomain
import id.app.instaapp.data.local.toEntity
import id.app.instaapp.data.model.AccessLevel
import id.app.instaapp.data.model.Comment
import id.app.instaapp.data.model.MediaType
import id.app.instaapp.data.model.NewPostPayload
import id.app.instaapp.data.model.Post
import id.app.instaapp.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val firebaseAuth: FirebaseAuth,
    private val postDao: PostDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PostRepository {

    override fun observePosts(): Flow<List<Post>> {
        return postDao.observePosts().map { cachedPosts ->
            val userId = firebaseAuth.currentUser?.uid
            cachedPosts
                .map { it.toDomain() }
                .filter { AccessControl.canView(it, userId) }
        }
    }

    override suspend fun refreshPosts(): Resource<Unit> = withContext(ioDispatcher) {
        try {
            val snapshot = firestore.collection(POSTS_COLLECTION)
                .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING)
                .get()
                .await()
            val userId = firebaseAuth.currentUser?.uid
            val posts = snapshot.documents.mapNotNull { it.toPost(userId) }
            postDao.clearPosts()
            postDao.insertPosts(posts.map { it.toEntity() })
            Resource.Success(Unit)
        } catch (error: Exception) {
            Resource.Error(error.message ?: "Unable to load posts", error)
        }
    }

    override suspend fun createPost(payload: NewPostPayload): Resource<Unit> = withContext(ioDispatcher) {
        val uid = firebaseAuth.currentUser?.uid
            ?: return@withContext Resource.Error("User not authenticated")
        val userName = firebaseAuth.currentUser?.displayName ?: firebaseAuth.currentUser?.email.orEmpty()
        return@withContext try {
            val mediaUrl = if (payload.mediaBytes != null) {
                val path = "posts/$uid/${UUID.randomUUID()}.${payload.fileExtension}"
                val reference = storage.reference.child(path)
                reference.putBytes(payload.mediaBytes).await()
                reference.downloadUrl.await().toString()
            } else {
                null
            }
            val doc = firestore.collection(POSTS_COLLECTION).document()
            val postMap = mapOf(
                "id" to doc.id,
                "userId" to uid,
                "userName" to userName,
                "userAvatarUrl" to firebaseAuth.currentUser?.photoUrl?.toString(),
                "caption" to payload.caption,
                "mediaUrl" to mediaUrl,
                "mediaType" to payload.mediaType.name,
                "likeCount" to 0,
                "commentCount" to 0,
                "likedUserIds" to emptyList<String>(),
                "accessLevel" to payload.accessLevel.name,
                "allowedUserIds" to payload.allowedUserIds,
                CREATED_AT_FIELD to System.currentTimeMillis()
            )
            doc.set(postMap).await()
            refreshPosts()
            Resource.Success(Unit)
        } catch (error: Exception) {
            Resource.Error(error.message ?: "Failed to create post", error)
        }
    }

    override suspend fun toggleLike(postId: String): Resource<Unit> = withContext(ioDispatcher) {
        val uid = firebaseAuth.currentUser?.uid
            ?: return@withContext Resource.Error("User not authenticated")
        val post = getPostInternal(postId)
            ?: return@withContext Resource.Error("Post not found")
        if (!AccessControl.canInteract(post, uid)) {
            return@withContext Resource.Error("You cannot interact with this post")
        }
        val docRef = firestore.collection(POSTS_COLLECTION).document(postId)
        return@withContext try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val likedUsers = snapshot.get(LIKED_USERS_FIELD) as? List<*> ?: emptyList<Any>()
                val alreadyLiked = likedUsers.contains(uid)
                if (alreadyLiked) {
                    transaction.update(docRef, mapOf(
                        LIKED_USERS_FIELD to FieldValue.arrayRemove(uid),
                        LIKE_COUNT_FIELD to FieldValue.increment(-1)
                    ))
                } else {
                    transaction.update(docRef, mapOf(
                        LIKED_USERS_FIELD to FieldValue.arrayUnion(uid),
                        LIKE_COUNT_FIELD to FieldValue.increment(1)
                    ))
                }
            }.await()
            refreshPosts()
            Resource.Success(Unit)
        } catch (error: Exception) {
            Resource.Error(error.message ?: "Failed to like post", error)
        }
    }

    override fun observeComments(postId: String): Flow<List<Comment>> = callbackFlow {
        val query = firestore.collection(POSTS_COLLECTION)
            .document(postId)
            .collection(COMMENTS_COLLECTION)
            .orderBy(CREATED_AT_FIELD, Query.Direction.ASCENDING)
        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            val comments = snapshot?.documents.orEmpty().map { doc ->
                Comment(
                    id = doc.id,
                    postId = postId,
                    userId = doc.getString("userId").orEmpty(),
                    userName = doc.getString("userName").orEmpty(),
                    text = doc.getString("text").orEmpty(),
                    createdAt = doc.getLong(CREATED_AT_FIELD) ?: System.currentTimeMillis()
                )
            }
            trySend(comments)
        }
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun addComment(postId: String, text: String): Resource<Unit> = withContext(ioDispatcher) {
        val uid = firebaseAuth.currentUser?.uid
            ?: return@withContext Resource.Error("User not authenticated")
        val post = getPostInternal(postId)
            ?: return@withContext Resource.Error("Post not found")
        if (!AccessControl.canInteract(post, uid)) {
            return@withContext Resource.Error("You cannot comment on this post")
        }
        val userName = firebaseAuth.currentUser?.displayName ?: firebaseAuth.currentUser?.email.orEmpty()
        val commentData = mapOf(
            "userId" to uid,
            "userName" to userName,
            "text" to text,
            CREATED_AT_FIELD to System.currentTimeMillis()
        )
        return@withContext try {
            firestore.collection(POSTS_COLLECTION)
                .document(postId)
                .collection(COMMENTS_COLLECTION)
                .add(commentData)
                .await()
            firestore.collection(POSTS_COLLECTION)
                .document(postId)
                .update(COMMENT_COUNT_FIELD, FieldValue.increment(1))
                .await()
            refreshPosts()
            Resource.Success(Unit)
        } catch (error: Exception) {
            Resource.Error(error.message ?: "Failed to comment", error)
        }
    }

    override suspend fun getPost(postId: String): Post? = withContext(ioDispatcher) {
        val userId = firebaseAuth.currentUser?.uid
        val snapshot = firestore.collection(POSTS_COLLECTION).document(postId).get().await()
        val post = snapshot.toPost(userId)
        if (post != null && AccessControl.canView(post, userId)) {
            post
        } else {
            null
        }
    }

    private suspend fun getPostInternal(postId: String): Post? {
        val userId = firebaseAuth.currentUser?.uid
        val doc = firestore.collection(POSTS_COLLECTION).document(postId).get().await()
        return doc.toPost(userId)
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toPost(currentUserId: String?): Post? {
        val ownerId = getString("userId") ?: return null
        val allowed = (get("allowedUserIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        val likedUsers = (get(LIKED_USERS_FIELD) as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        val likeCount = getLong(LIKE_COUNT_FIELD)?.toInt() ?: likedUsers.size
        val commentCount = getLong(COMMENT_COUNT_FIELD)?.toInt() ?: 0
        return Post(
            id = id,
            userId = ownerId,
            userName = getString("userName").orEmpty(),
            userAvatarUrl = getString("userAvatarUrl"),
            caption = getString("caption").orEmpty(),
            mediaUrl = getString("mediaUrl"),
            mediaType = MediaType.fromValue(getString("mediaType")),
            likeCount = likeCount,
            commentCount = commentCount,
            likedByCurrentUser = currentUserId != null && likedUsers.contains(currentUserId),
            accessLevel = AccessLevel.fromValue(getString("accessLevel")),
            allowedUserIds = allowed,
            createdAt = getLong(CREATED_AT_FIELD) ?: System.currentTimeMillis()
        )
    }

    companion object {
        private const val POSTS_COLLECTION = "posts"
        private const val COMMENTS_COLLECTION = "comments"
        private const val CREATED_AT_FIELD = "createdAt"
        private const val LIKED_USERS_FIELD = "likedUserIds"
        private const val LIKE_COUNT_FIELD = "likeCount"
        private const val COMMENT_COUNT_FIELD = "commentCount"
    }
}
