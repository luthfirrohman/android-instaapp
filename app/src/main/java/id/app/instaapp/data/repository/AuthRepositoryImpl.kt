package id.app.instaapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import id.app.instaapp.core.Resource
import id.app.instaapp.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    override val authState: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            val user = firebaseUser?.let {
                User(
                    id = it.uid,
                    name = it.displayName ?: it.email.orEmpty(),
                    email = it.email.orEmpty(),
                    avatarUrl = it.photoUrl?.toString()
                )
            }
            trySend(user)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun login(email: String, password: String): Resource<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(Unit)
        } catch (error: Exception) {
            Resource.Error(error.message ?: "Failed to login", error)
        }
    }

    override suspend fun register(name: String, email: String, password: String): Resource<Unit> {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            firebaseAuth.currentUser?.updateProfile(
                userProfileChangeRequest { displayName = name }
            )?.await()
            val uid = firebaseAuth.currentUser?.uid ?: return Resource.Error("User not created")
            val userDoc = mapOf(
                "name" to name,
                "email" to email,
                "avatarUrl" to firebaseAuth.currentUser?.photoUrl?.toString()
            )
            firestore.collection(USERS_COLLECTION).document(uid).set(userDoc).await()
            Resource.Success(Unit)
        } catch (error: Exception) {
            Resource.Error(error.message ?: "Failed to register", error)
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    companion object {
        private const val USERS_COLLECTION = "users"
    }
}