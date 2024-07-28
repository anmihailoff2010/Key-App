package com.example.keyapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.example.keyapp.R
import com.example.keyapp.databinding.FragmentLoginBinding
import com.example.keyapp.viewmodel.UserViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.checkUserExists()

        userViewModel.userExists.observe(viewLifecycleOwner, { exists ->
            if (!exists) {
                // User doesn't exist, navigate to registration screen
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }
        })

        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            userViewModel.loginUser(username, password)
        }

        userViewModel.user.observe(viewLifecycleOwner, { user ->
            if (user != null) {
                // User found, navigate to MainFragment
                findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
            } else {
                // User not found, show error message
                Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

