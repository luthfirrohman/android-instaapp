package id.app.instaapp.ui.postcreate

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import id.app.instaapp.R
import id.app.instaapp.core.Resource
import id.app.instaapp.data.model.AccessLevel
import id.app.instaapp.data.model.MediaType
import id.app.instaapp.databinding.FragmentCreatePostBinding
import id.app.instaapp.ui.extensions.appViewModels

class CreatePostFragment : Fragment() {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!
    private val viewModel by appViewModels<CreatePostViewModel>()
    private var accessLevel: AccessLevel = AccessLevel.PUBLIC

    private val pickMedia = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@registerForActivityResult
        try {
            requireActivity().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
        }
        viewModel.setSelectedMedia(requireContext(), uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSpinner()
        binding.selectMediaButton.setOnClickListener {
            pickMedia.launch(arrayOf("image/*", "video/*"))
        }
        binding.publishButton.setOnClickListener {
            val caption = binding.captionInput.text?.toString().orEmpty()
            viewModel.createPost(requireContext(), caption, accessLevel)
        }
        observeState()
    }

    private fun setupSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.access_levels,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            binding.accessSpinner.adapter = adapter
        }
        binding.accessSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                accessLevel = when (position) {
                    0 -> AccessLevel.PUBLIC
                    1 -> AccessLevel.FOLLOWERS
                    else -> AccessLevel.PRIVATE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeState() {
        viewModel.createState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.publishButton.isEnabled = false
                    binding.createProgress.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.publishButton.isEnabled = true
                    binding.createProgress.visibility = View.GONE
                    Toast.makeText(requireContext(), R.string.message_post_created, Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_createPostFragment_to_feedFragment)
                }
                is Resource.Error -> {
                    binding.publishButton.isEnabled = true
                    binding.createProgress.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
        viewModel.selectedMediaType.observe(viewLifecycleOwner) { type ->
            binding.mediaPreviewIndicator.visibility = if (type == MediaType.VIDEO && viewModel.selectedMediaUri.value != null) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
        viewModel.selectedMediaUri.observe(viewLifecycleOwner) { uri ->
            val type = viewModel.selectedMediaType.value ?: MediaType.IMAGE
            binding.mediaPreviewIndicator.visibility = if (type == MediaType.VIDEO && uri != null) View.VISIBLE else View.GONE
            when {
                uri == null -> binding.mediaPreview.setImageResource(R.mipmap.ic_launcher)
                type == MediaType.VIDEO -> Glide.with(this).load(uri).frame(1_000_000).into(binding.mediaPreview)
                else -> Glide.with(this).load(uri).into(binding.mediaPreview)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
