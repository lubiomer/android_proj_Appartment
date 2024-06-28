package com.management.roomates.view

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.management.roomates.CustomApplication
import com.management.roomates.R
import com.management.roomates.databinding.FragmentApartmentManagementBinding
import com.management.roomates.viewmodel.ApartmentManagementViewModel
import com.management.roomates.viewmodel.AuthViewModel
import com.management.roomates.viewmodel.AuthViewModelFactory

class ApartmentManagementFragment : Fragment() {

    private var _binding: FragmentApartmentManagementBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ApartmentManagementViewModel by viewModels()
    private lateinit var authViewModel: AuthViewModel

    private var flag = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val authViewModelFactory = AuthViewModelFactory(context)
        authViewModel = ViewModelProvider(this, authViewModelFactory).get(AuthViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentApartmentManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.manageGroceriesButton.setOnClickListener {
            findNavController().navigate(R.id.action_apartmentManagementFragment_to_groceriesFragment)
        }

        binding.manageBillingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_apartmentManagementFragment_to_billingsFragment)
        }

        binding.manageCleaningButton.setOnClickListener {
            findNavController().navigate(R.id.action_apartmentManagementFragment_to_cleaningFragment)
        }
        binding.weatherButton.setOnClickListener {
            findNavController().navigate(R.id.action_apartmentManagementFragment_to_weatherFragment)
        }

        binding.manageUpdatesButton.setOnClickListener {
            findNavController().navigate(R.id.action_apartmentManagementFragment_to_updatesFragment)
        }

        binding.logoutButton.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(R.id.loginFragment)
        }
    }

    override fun onResume() {
        viewModel.loadUserDetails().observe(viewLifecycleOwner) { user ->
            binding.helloText.text = "Hello ${user.name} !"
            CustomApplication.loggedUserApartmentId = user.apartmentId
            CustomApplication.loggedUserId = user.id


            viewModel.loadGrocery().observe(viewLifecycleOwner) { tasks ->
                val taskView = TextView(context)
                taskView.text = "Grocery Items"
                binding.tasksList.addView(taskView)
                tasks.forEach { task ->
//                    if (task.apartmentId == CustomApplication.loggedUserApartmentId) {
                        val taskView1 = TextView(context)
                        taskView1.setTextColor(Color.MAGENTA)
                        taskView1.text = "${task.name} – ${task.quantity}"
                        binding.tasksList.addView(taskView1)
//                    }
                }

                viewModel.loadCleaning().observe(viewLifecycleOwner) { cleaningShifts ->
                    val taskView = TextView(context)
                    taskView.text = "\nCleaning Shifts"
                    binding.tasksList.addView(taskView)
                    cleaningShifts.forEach { cleaningShift ->
//                    if (cleaningShift.apartmentId == CustomApplication.loggedUserApartmentId) {
                        val taskView1 = TextView(context)
                        taskView1.setTextColor(Color.BLUE)
                        taskView1.text =
                            "${cleaningShift.roommateName} – ${viewModel.convertTimestampToDateTimeString(cleaningShift.dateToClean)}"
                        binding.tasksList.addView(taskView1)
//                    }
                    }
                }
            }



            viewModel.loadUpdates(CustomApplication.loggedUserApartmentId).observe(viewLifecycleOwner) { updates ->
                updates.forEach { update ->
                    val updateView = TextView(context)
                    updateView.setTextColor(Color.RED)
                    updateView.text = "${update.note} : ${viewModel.convertTimestampToDateTimeString(update.date.toLong())}"
                    binding.updatesList.addView(updateView)
                }
            }
        }
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
