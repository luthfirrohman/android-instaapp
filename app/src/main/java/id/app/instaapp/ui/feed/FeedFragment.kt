package id.app.instaapp.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import id.app.instaapp.R
import id.app.instaapp.core.Resource
import id.app.instaapp.data.model.Post
import id.app.instaapp.databinding.FragmentFeedBinding
import id.app.instaapp.ui.auth.AuthViewModel
import id.app.instaapp.ui.extensions.appViewModels

class FeedFragment : Fragment(), PostInteractionListener {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private val feedViewModel by appViewModels<FeedViewModel>()
    private val authViewModel by appViewModels<AuthViewModel>()
    private lateinit var adapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = PostAdapter(this)
        binding.postsRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.postsRecyclerView.adapter = adapter
        binding.swipeRefresh.setOnRefreshListener { feedViewModel.refreshPosts() }
        binding.createPostFab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_createPostFragment)
        }
        binding.feedToolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.action_logout) {
                authViewModel.logout()
                Toast.makeText(requireContext(), R.string.message_logout_success, Toast.LENGTH_SHORT).show()
                true
            } else {
                false
            }
        }
        observeUi()
    }

    private fun observeUi() {
        feedViewModel.posts.observe(viewLifecycleOwner) { posts ->
            adapter.submitList(posts)
            binding.emptyStateText.isVisible = posts.isEmpty()
        }
        feedViewModel.refreshState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = state is Resource.Loading
            if (state is Resource.Error) {
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
            }
        }
        feedViewModel.interactionState.observe(viewLifecycleOwner) { state ->
            if (state is Resource.Error) {
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
            }
        }
        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user == null && findNavController().currentDestination?.id == R.id.feedFragment) {
                findNavController().navigate(R.id.action_feedFragment_to_loginFragment)
            }
        }
    }

    override fun onLike(post: Post) {
        feedViewModel.toggleLike(post.id)
    }

    override fun onComment(post: Post) {
        val directions = Bundle().apply {
            putString("postId", post.id)
            putString("userName", post.userName)
            putString("caption", post.caption)
            putString("mediaUrl", post.mediaUrl ?: "")
            putString("mediaType", post.mediaType.name)
        }
        findNavController().navigate(R.id.action_feedFragment_to_postDetailFragment, directions)
    }

    override fun onOpenMedia(post: Post) {
        val mediaUrl = post.mediaUrl?.takeIf { it.isNotBlank() } ?: return
        val args = Bundle().apply {
            putString("mediaUrl", mediaUrl)
            putString("mediaType", post.mediaType.name)
        }
//        findNavController().navigate(R.id.action_feedFragment_to_mediaViewerFragment, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
