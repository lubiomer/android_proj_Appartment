package com.management.roomates.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.management.roomates.R
import com.management.roomates.databinding.FragmentApartmentManagementBinding
import com.management.roomates.viewmodel.ApartmentManagementViewModel

class ApartmentManagementFragment : Fragment() {

    private var _binding: FragmentApartmentManagementBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ApartmentManagementViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentApartmentManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadUserDetails().observe(viewLifecycleOwner, { user ->
            binding.helloText.text = "Hello ${user.name} !"
        })

        viewModel.loadTasks().observe(viewLifecycleOwner, { tasks ->
            tasks.forEach { task ->
                val taskView = TextView(context)
                taskView.text = "${task.type} – ${task.assignedTo}"
                binding.tasksList.addView(taskView)
            }
        })

        viewModel.loadUpdates().observe(viewLifecycleOwner, { updates ->
            updates.forEach { update ->
                val updateView = TextView(context)
                updateView.text = "${update.date}: ${update.note}"
                binding.updatesList.addView(updateView)
            }
        })

        binding.manageGroceriesButton.setOnClickListener {
            //findNavController().navigate(R.id.action_apartmentManagementFragment_to_groceriesFragment)
        }

        binding.manageBillingsButton.setOnClickListener {
            //findNavController().navigate(R.id.action_apartmentManagementFragment_to_billingsFragment)
        }

        binding.manageCleaningButton.setOnClickListener {
            //findNavController().navigate(R.id.action_apartmentManagementFragment_to_cleaningFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
