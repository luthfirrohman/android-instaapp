package id.app.instaapp.ui.auth

import androidx.lifecycle.ViewModel
import id.app.instaapp.data.repository.AuthRepository
import javax.inject.Inject

class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {}