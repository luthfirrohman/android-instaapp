package id.app.instaapp.core

import id.app.instaapp.data.model.AccessLevel
import id.app.instaapp.data.model.Post

object AccessControl {
    fun canView(post: Post, userId: String?): Boolean {
        return when (post.accessLevel) {
            AccessLevel.PUBLIC -> true
            AccessLevel.FOLLOWERS -> post.userId == userId ||
                    (userId != null && post.allowedUserIds.contains(userId)) ||
                    post.allowedUserIds.isEmpty()
            AccessLevel.PRIVATE -> post.userId == userId
        }
    }

    fun canInteract(post: Post, userId: String?): Boolean = canView(post, userId)
}