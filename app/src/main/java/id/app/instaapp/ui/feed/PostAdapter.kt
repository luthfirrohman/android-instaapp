package id.app.instaapp.ui.feed

import android.content.res.ColorStateList
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.app.instaapp.R
import id.app.instaapp.data.model.AccessLevel
import id.app.instaapp.data.model.MediaType
import id.app.instaapp.data.model.Post
import id.app.instaapp.databinding.ItemPostBinding

interface PostInteractionListener {
    fun onLike(post: Post)
    fun onComment(post: Post)
    fun onOpenMedia(post: Post)
}

class PostAdapter(private val listener: PostInteractionListener) :
    ListAdapter<Post, PostAdapter.PostViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            val context = binding.root.context
            binding.userNameText.text = post.userName
            binding.accessLevelText.text = context.getString(post.accessLevel.toLabel())
            binding.captionText.text = post.caption
            binding.likesText.text = context.getString(R.string.label_likes, post.likeCount)
            binding.commentsText.text = context.getString(R.string.label_comments_count, post.commentCount)
            val relativeTime = DateUtils.getRelativeTimeSpanString(
                post.createdAt,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
            binding.postDateText.text = relativeTime

            val likeTint = if (post.likedByCurrentUser) {
                ContextCompat.getColor(context, R.color.color_secondary)
            } else {
                ContextCompat.getColor(context, R.color.color_on_surface)
            }
            binding.likeButton.imageTintList = ColorStateList.valueOf(likeTint)

            binding.mediaVideoIndicator.visibility = if (post.mediaType == MediaType.VIDEO && !post.mediaUrl.isNullOrEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
            if (post.mediaUrl.isNullOrEmpty()) {
                binding.mediaImage.setImageResource(R.mipmap.ic_launcher)
            } else {
                val request = Glide.with(binding.mediaImage)
                    .load(post.mediaUrl)
                    .centerCrop()
                if (post.mediaType == MediaType.VIDEO) {
                    request.frame(1_000_000).into(binding.mediaImage)
                } else {
                    request.into(binding.mediaImage)
                }
            }
            binding.likeButton.setOnClickListener { listener.onLike(post) }
            binding.commentButton.setOnClickListener { listener.onComment(post) }
            binding.mediaImage.setOnClickListener { listener.onOpenMedia(post) }
        }
    }

    private fun AccessLevel.toLabel(): Int {
        return when (this) {
            AccessLevel.PUBLIC -> R.string.option_access_public
            AccessLevel.FOLLOWERS -> R.string.option_access_followers
            AccessLevel.PRIVATE -> R.string.option_access_private
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
        }
    }
}
