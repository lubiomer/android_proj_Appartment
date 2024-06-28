package com.management.roomates.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.management.roomates.CustomApplication
import com.management.roomates.data.model.BillingItem
import com.management.roomates.databinding.FragmentBillingsBinding
import com.management.roomates.viewmodel.BillingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class BillingsFragment : Fragment() {
    private var _binding: FragmentBillingsBinding? = null
    private val binding get() = _binding!!
    private val billingsViewModel: BillingsViewModel by viewModels()
    private lateinit var billingsAdapter: BillingsAdapter

    private var editingBillingItem: BillingItem? = null
    private val calendar = Calendar.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBillingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        billingsAdapter = BillingsAdapter(billingsViewModel) { billingItem ->
            toggleEditBillingItem(billingItem)
        }
        binding.recyclerViewBillings.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = billingsAdapter
        }

        billingsViewModel.getAllBillings(CustomApplication.loggedUserApartmentId).observe(viewLifecycleOwner, Observer { billings ->
            billingsAdapter.submitList(billings)
        })

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDateInView()
        }

        binding.editTextDate.setOnClickListener {
            DatePickerDialog(
                requireContext(), dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.buttonAddBilling.setOnClickListener {
            val date = binding.editTextDate.text.toString()
            val amount = binding.editTextAmount.text.toString().toDoubleOrNull() ?: 0.0
            val status = if (binding.switchStatus.isChecked) "Paid" else "Unpaid"
            val lastUpdatedBy = auth.currentUser?.email ?: "Unknown"
            val lastUpdatedAt = System.currentTimeMillis()

            if (date.isNotEmpty() && amount > 0) {
                fetchApartmentId { apartmentId ->
                    if (apartmentId != null) {
                        var billingItem = BillingItem(
                            date = date,
                            amount = amount,
                            status = status,
                            lastUpdatedBy = lastUpdatedBy,
                            lastUpdatedAt = lastUpdatedAt,
                            apartmentId = apartmentId
                        )

                        if (editingBillingItem == null) {
                            billingsViewModel.insert(billingItem)
                            binding.buttonAddBilling.text = "Add Billing"
                        } else {
                            billingItem.id = editingBillingItem!!.id
                            billingsViewModel.update(billingItem)
                            editingBillingItem = null
                            binding.buttonAddBilling.text = "Add Billing"
                        }

                        binding.editTextDate.text?.clear()
                        binding.editTextAmount.text?.clear()
                        binding.switchStatus.isChecked = false
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch apartment name", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please enter valid date and amount", Toast.LENGTH_SHORT).show()
            }
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
                    return document.id // or document.getString("address")
                }
            }
        }
        return null
    }


    private fun toggleEditBillingItem(billingItem: BillingItem) {
        if (editingBillingItem == billingItem) {
            clearEditingState()
        } else {
            binding.editTextDate.setText(billingItem.date)
            binding.editTextAmount.setText(billingItem.amount.toString())
            binding.switchStatus.isChecked = billingItem.status == "Paid"
            editingBillingItem = billingItem
            binding.buttonAddBilling.text = "Update Billing"
            billingsAdapter.setEditingBillingItemId(billingItem.id)
        }
    }

    private fun clearEditingState() {
        editingBillingItem = null
        binding.editTextDate.text?.clear()
        binding.editTextAmount.text?.clear()
        binding.switchStatus.isChecked = false
        binding.buttonAddBilling.text = "Add Billing"
        billingsAdapter.setEditingBillingItemId(null)
    }

    private fun updateDateInView() {
        val myFormat = "yyyy-MM-dd" // Specify your format
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.editTextDate.setText(sdf.format(calendar.time))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
