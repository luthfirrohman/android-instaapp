package id.app.instaapp.ui.mediaviewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import id.app.instaapp.R
import id.app.instaapp.data.model.MediaType
import id.app.instaapp.databinding.FragmentMediaViewerBinding

class MediaViewerFragment : Fragment() {

    private var _binding: FragmentMediaViewerBinding? = null
    private val binding get() = _binding!!
    private var mediaType: MediaType = MediaType.IMAGE
    private var mediaUrl: String = ""
    private var player: ExoPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMediaViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewerToolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.viewerToolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        parseArgs()
    }

    private fun parseArgs() {
        val args = requireArguments()
        mediaUrl = args.getString(ARG_MEDIA_URL).orEmpty()
        mediaType = MediaType.fromValue(args.getString(ARG_MEDIA_TYPE))
        if (mediaUrl.isBlank()) {
            findNavController().navigateUp()
            return
        }

        if (mediaType == MediaType.VIDEO) {
            binding.viewerImage.visibility = View.GONE
            binding.viewerVideo.visibility = View.VISIBLE
            binding.viewerLoading.visibility = View.VISIBLE
            initializePlayer(mediaUrl)
        } else {
            binding.viewerVideo.visibility = View.GONE
            binding.viewerImage.visibility = View.VISIBLE
            Glide.with(this).load(mediaUrl).into(binding.viewerImage)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
        _binding = null
    }

    companion object {
        const val ARG_MEDIA_URL = "mediaUrl"
        const val ARG_MEDIA_TYPE = "mediaType"
    }

    private fun initializePlayer(url: String) {
        releasePlayer()
        player = ExoPlayer.Builder(requireContext()).build().also { exoPlayer ->
            binding.viewerVideo.player = exoPlayer
            val mediaItem = MediaItem.fromUri(url)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.playWhenReady = true
            exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ONE
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED) {
                        binding.viewerLoading.visibility = View.GONE
                    }
                }

                override fun onPlayerError(error: com.google.android.exoplayer2.PlaybackException) {
                    binding.viewerLoading.visibility = View.GONE
                    Toast.makeText(requireContext(), R.string.message_video_error, Toast.LENGTH_SHORT).show()
                }
            })
            exoPlayer.prepare()
        }
    }

    private fun releasePlayer() {
        player?.release()
        player = null
        binding.viewerVideo.player = null
    }
}
