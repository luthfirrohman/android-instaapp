package id.app.instaapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import id.app.instaapp.R
import id.app.instaapp.core.Resource
import id.app.instaapp.databinding.FragmentLoginBinding
import id.app.instaapp.ui.extensions.appViewModels

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val authViewModel by appViewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text?.toString().orEmpty()
            val password = binding.passwordInput.text?.toString().orEmpty()
            authViewModel.login(email, password)
        }
        binding.registerLink.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        observeState()
    }

    private fun observeState() {
        authViewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.loginButton.isEnabled = false
                    binding.loginProgress.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.loginButton.isEnabled = true
                    binding.loginProgress.visibility = View.GONE
                    findNavController().navigate(R.id.action_loginFragment_to_feedFragment)
                }
                is Resource.Error -> {
                    binding.loginButton.isEnabled = true
                    binding.loginProgress.visibility = View.GONE
                    Toast.makeText(requireContext(), "Kredensial salah", Toast.LENGTH_SHORT).show()
                }
                null -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
