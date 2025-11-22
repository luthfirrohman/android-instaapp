package id.app.instaapp.data.model

data class Comment(
    val id: String,
    val postId: String,
    val userId: String,
    val userName: String,
    val text: String,
    val createdAt: Long
)