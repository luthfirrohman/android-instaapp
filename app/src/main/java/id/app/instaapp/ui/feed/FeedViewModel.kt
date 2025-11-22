package id.app.instaapp.ui.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import id.app.instaapp.core.Resource
import id.app.instaapp.data.model.Post
import id.app.instaapp.data.repository.PostRepository
import javax.inject.Inject
import kotlinx.coroutines.launch

class FeedViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    val posts: LiveData<List<Post>> = postRepository.observePosts().asLiveData()

    private val _refreshState = MutableLiveData<Resource<Unit>>()
    val refreshState: LiveData<Resource<Unit>> = _refreshState

    private val _interactionState = MutableLiveData<Resource<Unit>>()
    val interactionState: LiveData<Resource<Unit>> = _interactionState

    init {
        refreshPosts()
    }

    fun refreshPosts() {
        _refreshState.value = Resource.Loading
        viewModelScope.launch {
            _refreshState.value = postRepository.refreshPosts()
        }
    }

    fun toggleLike(postId: String) {
        _interactionState.value = Resource.Loading
        viewModelScope.launch {
            _interactionState.value = postRepository.toggleLike(postId)
        }
    }
}
