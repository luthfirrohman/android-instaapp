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
import id.app.instaapp.databinding.FragmentRegisterBinding
import id.app.instaapp.ui.extensions.appViewModels

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val authViewModel by appViewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.registerButton.setOnClickListener {
            val name = binding.nameInput.text?.toString().orEmpty()
            val email = binding.registerEmailInput.text?.toString().orEmpty()
            val password = binding.registerPasswordInput.text?.toString().orEmpty()
            authViewModel.register(name, email, password)
        }
        binding.loginLink.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
        observeState()
    }

    private fun observeState() {
        authViewModel.registerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.registerButton.isEnabled = false
                    binding.registerProgress.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.registerButton.isEnabled = true
                    binding.registerProgress.visibility = View.GONE
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                }
                is Resource.Error -> {
                    binding.registerButton.isEnabled = true
                    binding.registerProgress.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
