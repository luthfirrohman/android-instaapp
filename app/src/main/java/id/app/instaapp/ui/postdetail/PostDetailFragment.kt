package id.app.instaapp.ui.postdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import id.app.instaapp.R
import id.app.instaapp.core.Resource
import id.app.instaapp.data.model.MediaType
import id.app.instaapp.databinding.FragmentPostDetailBinding
import id.app.instaapp.ui.extensions.appViewModels

class PostDetailFragment : Fragment() {

    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel by appViewModels<PostDetailViewModel>()
    private val commentAdapter = CommentAdapter()
    private lateinit var postId: String
    private var mediaType: MediaType = MediaType.IMAGE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.commentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.commentsRecyclerView.adapter = commentAdapter
        binding.detailToolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.detailToolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        parseArgs()
        binding.sendCommentButton.setOnClickListener {
            val text = binding.commentInput.text?.toString().orEmpty()
            if (text.isNotBlank()) {
                viewModel.addComment(postId, text)
                binding.commentInput.setText("")
            }
        }
        observeData()
    }

    private fun parseArgs() {
        val args = requireArguments()
        postId = args.getString("postId").orEmpty()
        if (postId.isBlank()) {
            Toast.makeText(requireContext(), R.string.action_retry, Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }
        binding.detailUser.text = args.getString("userName", "")
        binding.detailCaption.text = args.getString("caption", "")
        mediaType = MediaType.fromValue(args.getString("mediaType"))
        val mediaUrl = args.getString("mediaUrl")
        val hasMedia = !mediaUrl.isNullOrEmpty()
        binding.detailVideoIndicator.visibility = if (mediaType == MediaType.VIDEO && hasMedia) View.VISIBLE else View.GONE
        if (hasMedia) {
            if (mediaType == MediaType.VIDEO) {
                Glide.with(this).load(mediaUrl).frame(1_000_000).into(binding.detailMedia)
            } else {
                Glide.with(this).load(mediaUrl).into(binding.detailMedia)
            }
        } else {
            binding.detailMedia.setImageResource(android.R.drawable.ic_menu_report_image)
        }
        binding.detailMedia.setOnClickListener {
            val targetUrl = mediaUrl
            if (!targetUrl.isNullOrBlank()) {
                val argsBundle = Bundle().apply {
                    putString("mediaUrl", targetUrl)
                    putString("mediaType", mediaType.name)
                }
                findNavController().navigate(R.id.action_postDetailFragment_to_mediaViewerFragment, argsBundle)
            }
        }
        viewModel.startObserving(postId)
    }

    private fun observeData() {
        viewModel.comments.observe(viewLifecycleOwner) { comments ->
            commentAdapter.submitList(comments)
        }
        viewModel.commentState.observe(viewLifecycleOwner) { state ->
            if (state is Resource.Error) {
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
