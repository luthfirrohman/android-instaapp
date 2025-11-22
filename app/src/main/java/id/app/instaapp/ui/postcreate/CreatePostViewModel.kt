package id.app.instaapp.ui.postcreate

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.app.instaapp.core.Resource
import id.app.instaapp.data.model.AccessLevel
import id.app.instaapp.data.model.MediaType
import id.app.instaapp.data.model.NewPostPayload
import id.app.instaapp.data.repository.PostRepository
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _selectedMediaUri = MutableLiveData<Uri?>()
    val selectedMediaUri: LiveData<Uri?> = _selectedMediaUri

    private val _selectedMediaType = MutableLiveData(MediaType.IMAGE)
    val selectedMediaType: LiveData<MediaType> = _selectedMediaType

    private val _createState = MutableLiveData<Resource<Unit>>()
    val createState: LiveData<Resource<Unit>> = _createState

    fun setSelectedMedia(context: Context, uri: Uri?) {
        _selectedMediaUri.value = uri
        _selectedMediaType.value = detectMediaType(context, uri)
    }

    fun createPost(context: Context, caption: String, accessLevel: AccessLevel) {
        val uri = _selectedMediaUri.value
        val mediaType = _selectedMediaType.value ?: MediaType.IMAGE
        _createState.value = Resource.Loading
        viewModelScope.launch {
            val bytes = withContext(Dispatchers.IO) { readBytes(context, uri) }
            val payload = NewPostPayload(
                caption = caption,
                mediaBytes = bytes,
                mediaType = mediaType,
                fileExtension = resolveExtension(context, uri, mediaType),
                accessLevel = accessLevel
            )
            _createState.value = postRepository.createPost(payload)
        }
    }

    fun clearSelection() {
        _selectedMediaUri.value = null
        _selectedMediaType.value = MediaType.IMAGE
    }

    private fun detectMediaType(context: Context, uri: Uri?): MediaType {
        if (uri == null) return MediaType.IMAGE
        val mimeType = context.contentResolver.getType(uri)
        return when {
            mimeType?.startsWith("video") == true -> MediaType.VIDEO
            else -> MediaType.IMAGE
        }
    }

    private fun resolveExtension(context: Context, uri: Uri?, mediaType: MediaType): String {
        val defaultExtension = if (mediaType == MediaType.VIDEO) "mp4" else "jpg"
        if (uri == null) return defaultExtension
        val mimeType = context.contentResolver.getType(uri) ?: return defaultExtension
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: defaultExtension
    }

    private fun readBytes(context: Context, uri: Uri?): ByteArray? {
        if (uri == null) return null
        return context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes()
        }
    }
}