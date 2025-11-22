package id.app.instaapp.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import id.app.instaapp.core.Resource
import id.app.instaapp.data.model.User
import id.app.instaapp.data.repository.AuthRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: LiveData<User?> = authRepository.authState.asLiveData()

    private val _loginState = MutableLiveData<Resource<Unit>>()
    val loginState: LiveData<Resource<Unit>> = _loginState

    private val _registerState = MutableLiveData<Resource<Unit>>()
    val registerState: LiveData<Resource<Unit>> = _registerState

    fun login(email: String, password: String) {
        _loginState.value = Resource.Loading
        viewModelScope.launch {
            _loginState.value = authRepository.login(email, password)
        }
    }

    fun register(name: String, email: String, password: String) {
        _registerState.value = Resource.Loading
        viewModelScope.launch {
            _registerState.value = authRepository.register(name, email, password)
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}