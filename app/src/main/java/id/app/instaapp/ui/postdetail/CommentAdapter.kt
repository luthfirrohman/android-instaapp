package id.app.instaapp.ui.postdetail

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.app.instaapp.data.model.Comment
import id.app.instaapp.databinding.ItemCommentBinding

class CommentAdapter : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private val items = mutableListOf<Comment>()

    fun submitList(data: List<Comment>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class CommentViewHolder(private val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            binding.commentUser.text = comment.userName
            binding.commentText.text = comment.text
            val relativeTime = DateUtils.getRelativeTimeSpanString(
                comment.createdAt,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
            binding.commentTimestamp.text = relativeTime
        }
    }
}
