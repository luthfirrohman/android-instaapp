package id.app.instaapp.ui.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import id.app.instaapp.R
import id.app.instaapp.ui.auth.AuthViewModel
import id.app.instaapp.ui.extensions.appViewModels

class SplashFragment : Fragment(R.layout.fragment_splash) {

    private val authViewModel by appViewModels<AuthViewModel>()
    private var navigated = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (navigated) return@observe
            val destination = if (user == null) {
                R.id.action_splashFragment_to_loginFragment
            } else {
                R.id.action_splashFragment_to_feedFragment
            }
            navigated = true
            findNavController().navigate(destination)
        }
    }
}
