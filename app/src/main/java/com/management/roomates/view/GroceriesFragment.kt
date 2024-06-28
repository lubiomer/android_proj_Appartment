package com.management.roomates.view

import android.R
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
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
import com.management.roomates.data.model.GroceryItem
import com.management.roomates.databinding.FragmentGroceriesBinding
import com.management.roomates.viewmodel.GroceriesViewModel
import com.management.roomates.viewmodel.GroceriesViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.management.roomates.CustomApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GroceriesFragment : Fragment() {
    private var _binding: FragmentGroceriesBinding? = null
    private val binding get() = _binding!!
    private val groceriesViewModel: GroceriesViewModel by viewModels {
        GroceriesViewModelFactory(requireContext())
    }
    private lateinit var groceriesAdapter: GroceriesAdapter
    private lateinit var purchasesAdapter: PurchasesAdapter

    private var editingGroceryItem: GroceryItem? = null
    private val auth = FirebaseAuth.getInstance()
    private val calendar = Calendar.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGroceriesBinding.inflate(inflater, container, false).apply {
            viewModel = groceriesViewModel
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }
    private fun updateDateInView() {
        val myFormat = "yyyy-MM-dd" // Specify your format
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.editTextDate.setText(sdf.format(calendar.time))
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        setupRecyclerView()
        setupObservers()
        setupAddGroceryButton()
    }

    private fun setupRecyclerView() {
        groceriesAdapter = GroceriesAdapter(groceriesViewModel) { groceryItem ->
            editGroceryItem(groceryItem)
        }
        purchasesAdapter = PurchasesAdapter()

        binding.recyclerViewGroceries.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groceriesAdapter
        }

        binding.recyclerViewUpcomingPurchases.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = purchasesAdapter
        }
    }

    private fun setupObservers() {
        groceriesViewModel.allGroceries.observe(viewLifecycleOwner, Observer { groceries ->
            groceriesAdapter.submitList(groceries)
            if (groceries != null && groceries.isNotEmpty()){
//                if (purchases.isNotEmpty()){
                val item = groceries[groceries.size -1]
                binding.textLastUpdated.text = "Last Updated by ${item.lastUpdatedBy}"
//                }
            }
        })

        groceriesViewModel.getUsersByApartmentId(CustomApplication.loggedUserApartmentId) { users ->
            val adapter = ArrayAdapter(requireActivity(), R.layout.simple_list_item_1, users)
            binding.editTextRoommateName.setAdapter(adapter)
        }

        groceriesViewModel.upcomingPurchases.observe(viewLifecycleOwner, Observer { purchases ->
            purchasesAdapter.submitList(purchases)

        })
    }

    private fun setupAddGroceryButton() {
        binding.buttonAddGrocery.setOnClickListener {
            if (editingGroceryItem == null) {
                addGroceryItem()
            } else {
                updateGroceryItem()
            }
        }
    }

    private fun addGroceryItem() {
        val assignedTo = binding.editTextRoommateName.text.toString()
        val itemName = binding.editTextItemName.text.toString()
        val quantity = binding.editTextQuantity.text.toString().toIntOrNull()
        val lastUpdatedBy = auth.currentUser?.email ?: "Unknown"
        val lastUpdatedAt = System.currentTimeMillis()
        val date = binding.editTextDate.text.toString()
        if (itemName.isNotEmpty() && quantity != null && quantity > 0) {
            groceriesViewModel.apartmentId.observe(viewLifecycleOwner, Observer { apartmentId ->

                if (apartmentId != null) {
                    val groceryItem = GroceryItem(
                        name = itemName,
                        date = date,
                        quantity = quantity,
                        lastUpdatedBy = lastUpdatedBy,
                        lastUpdatedAt = lastUpdatedAt,
                        apartmentId = apartmentId,
                        assignedTo = assignedTo
                    )

                    groceriesViewModel.insert(groceryItem)
                    clearEditingState()
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch apartment name", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(requireContext(), "Please enter a valid item name and quantity", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateGroceryItem() {
        val assignedTo = binding.editTextRoommateName.text.toString()
        val itemName = binding.editTextItemName.text.toString()
        val quantity = binding.editTextQuantity.text.toString().toIntOrNull()
        val lastUpdatedBy = auth.currentUser?.email ?: "Unknown"
        val lastUpdatedAt = System.currentTimeMillis()
        val date = binding.editTextDate.text.toString()
        if (itemName.isNotEmpty() && quantity != null && quantity > 0) {
            groceriesViewModel.apartmentId.observe(viewLifecycleOwner, Observer { apartmentId ->
                if (apartmentId != null) {
                    val groceryItem = GroceryItem(
                        id = editingGroceryItem!!.id,
                        date = date,
                        name = itemName,
                        quantity = quantity,
                        lastUpdatedBy = lastUpdatedBy,
                        lastUpdatedAt = lastUpdatedAt,
                        apartmentId = apartmentId,
                        assignedTo  = assignedTo
                    )

                    groceriesViewModel.update(groceryItem)
                    clearEditingState()
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch apartment name", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(requireContext(), "Please enter a valid item name and quantity", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editGroceryItem(groceryItem: GroceryItem) {
        binding.editTextRoommateName.setText(groceryItem.assignedTo)
        binding.editTextItemName.setText(groceryItem.name)
        binding.editTextDate.setText(groceryItem.date)
        binding.editTextQuantity.setText(groceryItem.quantity.toString())
        editingGroceryItem = groceryItem
        groceriesAdapter.setEditingGroceryItemId(groceryItem.id)
        binding.buttonAddGrocery.text = "Update"
    }

    private fun clearEditingState() {
        binding.editTextRoommateName.text?.clear()
        binding.editTextItemName.text?.clear()
        binding.editTextDate.text?.clear()
        binding.editTextQuantity.text?.clear()
        binding.buttonAddGrocery.text = "Add"
        editingGroceryItem = null
        groceriesAdapter.setEditingGroceryItemId(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
