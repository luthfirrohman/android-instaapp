package id.app.instaapp.data.repository

import id.app.instaapp.core.Resource
import id.app.instaapp.data.model.Comment
import id.app.instaapp.data.model.NewPostPayload
import id.app.instaapp.data.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    suspend fun refreshPosts(): Resource<Unit>
    suspend fun createPost(payload: NewPostPayload): Resource<Unit>
    suspend fun getPost(postId: String): Post?
    fun observePosts(): Flow<List<Post>>
    suspend fun toggleLike(postId: String): Resource<Unit>
    suspend fun addComment(postId: String, text: String): Resource<Unit>
    fun observeComments(postId: String): Flow<List<Comment>>
}