package com.management.roomates.view

import android.R
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.management.roomates.data.AppDatabase
import com.management.roomates.data.model.CleaningShift
import com.management.roomates.databinding.FragmentCleaningShiftBinding
import com.management.roomates.viewmodel.CleaningShiftViewModel
import com.management.roomates.viewmodel.CleaningShiftViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.management.roomates.CustomApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CleaningShiftFragment : Fragment() {
    private var _binding: FragmentCleaningShiftBinding? = null
    private val binding get() = _binding!!

    private val cleaningShiftViewModel: CleaningShiftViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        CleaningShiftViewModelFactory(database.cleaningShiftDao(), "currentUserId") // currentUserId는 실제로 사용되는 ID로 대체해야 합니다.
    }

    private lateinit var cleaningShiftAdapter: CleaningShiftAdapter
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCleaningShiftBinding.inflate(inflater, container, false).apply {
            viewModel = cleaningShiftViewModel
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cleaningShiftAdapter = CleaningShiftAdapter(cleaningShiftViewModel) { cleaningShift ->
            toggleEditCleaningShift(cleaningShift)
        }

        binding.recyclerViewCleaningShifts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cleaningShiftAdapter
        }

        cleaningShiftViewModel.allCleaningShifts.observe(viewLifecycleOwner, Observer { cleaningShifts ->
            cleaningShiftAdapter.submitList(cleaningShifts)
        })

        cleaningShiftViewModel.getUsersByApartmentId(CustomApplication.loggedUserApartmentId) { users ->
            val adapter = ArrayAdapter(requireActivity(), R.layout.simple_list_item_1, users)
            binding.editTextRoommateName.setAdapter(adapter)
        }

        cleaningShiftViewModel.currentCleaningShift.observe(viewLifecycleOwner, Observer { cleaningShift ->
            if (cleaningShift != null) {
                binding.editTextRoommateName.setText(cleaningShift.roommateName)
                binding.editTextDateToClean.setText(dateFormat.format(Date(cleaningShift.dateToClean)))
                binding.buttonAddCleaningShift.setText("Update")
            } else {
                binding.editTextRoommateName.text?.clear()
                binding.editTextDateToClean.text?.clear()
                binding.buttonAddCleaningShift.setText("Add")
            }
            cleaningShiftAdapter.setEditingShiftId(cleaningShift?.id)
        })

        binding.buttonAddCleaningShift.setOnClickListener {
            val roommateName = binding.editTextRoommateName.text.toString()
            val dateToClean = calendar.timeInMillis
            val lastUpdatedBy = auth.currentUser?.email ?: "Unknown"
            val lastUpdatedAt = System.currentTimeMillis()

            if (roommateName.isNotEmpty()) {
                fetchApartmentId { apartmentId ->
                    if (apartmentId != null) {
                        val cleaningShift = CleaningShift(
                            roommateName = roommateName,
                            dateToClean = dateToClean,
                            lastUpdatedBy = lastUpdatedBy,
                            lastUpdatedAt = lastUpdatedAt,
                            apartmentId = apartmentId
                        )

                        if (cleaningShiftViewModel.currentCleaningShift.value == null) {
                            cleaningShiftViewModel.insert(cleaningShift)
                        } else {
                            cleaningShift.id = cleaningShiftViewModel.currentCleaningShift.value!!.id
                            cleaningShiftViewModel.update(cleaningShift)
                        }

                        cleaningShiftViewModel.clearCurrentCleaningShift()
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch apartment name", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a valid roommate name and date to clean", Toast.LENGTH_SHORT).show()
            }
        }

        binding.editTextDateToClean.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun fetchApartmentId(callback: (String?) -> Unit) {
        lifecycleScope.launch {
            val apartmentId = getApartmentIdForCurrentUser()
            withContext(Dispatchers.Main) {
                callback(apartmentId)
            }
        }
    }

    private suspend fun getApartmentIdForCurrentUser(): String? {
        val user = auth.currentUser ?: return null
        val userEmail = user.email ?: return null

        val apartments = firestore.collection("apartments")
            .get()
            .await()

        for (document in apartments.documents) {
            val roommates = document.get("roommates") as? List<Map<String, Any>> ?: continue
            for (roommate in roommates) {
                if (roommate["email"] == userEmail) {
                    return document.id // or document.getString("address") or document.getString("name") if those fields exist
                }
            }
        }
        return null
    }

    private fun showDatePickerDialog() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            binding.editTextDateToClean.setText(dateFormat.format(calendar.time))
        }

        DatePickerDialog(
            requireContext(),
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun toggleEditCleaningShift(cleaningShift: CleaningShift) {
        if (cleaningShiftViewModel.currentCleaningShift.value == cleaningShift) {
            cleaningShiftViewModel.clearCurrentCleaningShift()
        } else {
            cleaningShiftViewModel.setCurrentCleaningShift(cleaningShift)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
