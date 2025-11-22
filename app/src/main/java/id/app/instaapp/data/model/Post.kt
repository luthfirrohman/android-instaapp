package id.app.instaapp.data.model

data class Post(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatarUrl: String?,
    val caption: String,
    val mediaUrl: String?,
    val mediaType: MediaType,
    val likeCount: Int,
    val commentCount: Int,
    val likedByCurrentUser: Boolean,
    val accessLevel: AccessLevel,
    val allowedUserIds: List<String>,
    val createdAt: Long
)