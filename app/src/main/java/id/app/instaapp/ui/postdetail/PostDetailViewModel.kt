package id.app.instaapp.ui.postdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.app.instaapp.core.Resource
import id.app.instaapp.data.model.Comment
import id.app.instaapp.data.repository.PostRepository
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _comments = MutableLiveData<List<Comment>>(emptyList())
    val comments: LiveData<List<Comment>> = _comments

    private val _commentState = MutableLiveData<Resource<Unit>>()
    val commentState: LiveData<Resource<Unit>> = _commentState

    private var commentsJob: Job? = null

    fun startObserving(postId: String) {
        commentsJob?.cancel()
        commentsJob = viewModelScope.launch {
            postRepository.observeComments(postId).collectLatest { list ->
                _comments.postValue(list)
            }
        }
    }

    fun addComment(postId: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        _commentState.value = Resource.Loading
        viewModelScope.launch {
            _commentState.value = postRepository.addComment(postId, trimmed)
        }
    }

    override fun onCleared() {
        commentsJob?.cancel()
        super.onCleared()
    }
}
