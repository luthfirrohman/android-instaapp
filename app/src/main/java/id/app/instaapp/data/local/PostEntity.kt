package id.app.instaapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import id.app.instaapp.data.model.AccessLevel
import id.app.instaapp.data.model.MediaType
import id.app.instaapp.data.model.Post

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val userAvatarUrl: String?,
    val caption: String,
    val mediaUrl: String?,
    val mediaType: String,
    val likeCount: Int,
    val commentCount: Int,
    val likedByCurrentUser: Boolean,
    val accessLevel: String,
    val allowedUserIds: List<String>,
    val createdAt: Long
)

fun PostEntity.toDomain(): Post = Post(
    id = id,
    userId = userId,
    userName = userName,
    userAvatarUrl = userAvatarUrl,
    caption = caption,
    mediaUrl = mediaUrl,
    mediaType = MediaType.fromValue(mediaType),
    likeCount = likeCount,
    commentCount = commentCount,
    likedByCurrentUser = likedByCurrentUser,
    accessLevel = AccessLevel.fromValue(accessLevel),
    allowedUserIds = allowedUserIds,
    createdAt = createdAt
)

fun Post.toEntity(): PostEntity = PostEntity(
    id = id,
    userId = userId,
    userName = userName,
    userAvatarUrl = userAvatarUrl,
    caption = caption,
    mediaUrl = mediaUrl,
    mediaType = mediaType.name,
    likeCount = likeCount,
    commentCount = commentCount,
    likedByCurrentUser = likedByCurrentUser,
    accessLevel = accessLevel.name,
    allowedUserIds = allowedUserIds,
    createdAt = createdAt
)
