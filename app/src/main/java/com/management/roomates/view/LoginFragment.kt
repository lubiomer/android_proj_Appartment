package com.management.roomates.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.management.roomates.R
import com.management.roomates.databinding.FragmentLoginBinding
import com.management.roomates.viewmodel.AuthViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonLogin.setOnClickListener {
            authViewModel.email.value = binding.username.text.toString().trim()
            authViewModel.password.value = binding.password.text.toString().trim()
            if (authViewModel.email.value!!.isNotEmpty() && authViewModel.password.value!!.isNotEmpty()) {
                authViewModel.login().observe(viewLifecycleOwner, Observer { result ->
                    if (result.isSuccess) {
                        Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_LONG).show()
                        findNavController().navigate(R.id.action_loginFragment_to_apartmentManagementFragment)
                    } else {
                        Toast.makeText(requireContext(), "Login Failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                    }
                })
            } else {
                Toast.makeText(requireContext(), "Please enter your information", Toast.LENGTH_LONG).show()
            }
        }

        binding.buttonRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
