package id.app.instaapp.data.repository

import id.app.instaapp.core.Resource
import id.app.instaapp.data.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: String?
    val authState: Flow<User?>
    suspend fun register(name: String, email: String, password: String): Resource<Unit>
    suspend fun login(email: String, password: String): Resource<Unit>
    suspend fun logout()
}