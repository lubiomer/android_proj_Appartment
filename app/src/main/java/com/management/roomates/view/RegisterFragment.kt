package com.management.roomates.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.firestore.FirebaseFirestore
import com.management.roomates.R
import com.management.roomates.data.model.User
import com.management.roomates.databinding.FragmentRegisterBinding
import com.management.roomates.viewmodel.AuthViewModel
import com.management.roomates.viewmodel.AuthViewModelFactory

class RegisterFragment : Fragment() {

    private companion object {
        const val AUTOCOMPLETE_REQUEST_CODE = 1
    }

    private lateinit var firestore: FirebaseFirestore
    private lateinit var addressEditText: EditText
    private lateinit var roommatesContainer: LinearLayout
    private lateinit var numberOfRoommates: EditText
    private lateinit var sharedPasswordEditText: EditText

    private val authViewModel: AuthViewModel by viewModels() {
        AuthViewModelFactory(requireContext())
    }

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        addressEditText = binding.address
        roommatesContainer = binding.roommatesContainer
        numberOfRoommates = binding.number
        sharedPasswordEditText = binding.password

        addressEditText.setOnClickListener { startAutocomplete() }
        binding.finishRegister.setOnClickListener { registerUser() }
        binding.registerCancel.setOnClickListener { findNavController().popBackStack() }

        numberOfRoommates.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateRoommateFields()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key))
        }
    }

    private fun startAutocomplete() {
        val fields = listOf(com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.NAME)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(requireContext())
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    private fun updateRoommateFields() {
        val count = numberOfRoommates.text.toString().toIntOrNull() ?: return
        roommatesContainer.removeAllViews()
        for (i in 0 until count) {
            val roommateView = layoutInflater.inflate(R.layout.roommate_field, null)
            roommateView.findViewById<TextView>(R.id.roommateLabel).text = "Roommate details: ${i + 1}"
            roommatesContainer.addView(roommateView)
        }
    }

    private fun registerUser() {
        val address = addressEditText.text.toString().trim()
        val sharedPassword = sharedPasswordEditText.text.toString().trim()

        registerRoommates(sharedPassword, address)

    }

    private fun registerRoommates(sharedPassword: String, address: String) {
        val roommates = mutableListOf<Map<String, String>>()

        val ref = firestore.collection("apartments").document()
        val apartment = hashMapOf(
            "id" to ref.id,
            "address" to address,
            "sharedPassword" to sharedPassword,
//            "roommates" to roommates
        )

        firestore.collection("apartments")
            .document(ref.id).set(apartment)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Apartment registered successfully", Toast.LENGTH_SHORT).show()

                for (i in 0 until roommatesContainer.childCount) {
                    val roommateView = roommatesContainer.getChildAt(i)
                    val roommateName = roommateView.findViewById<EditText>(R.id.roommateName).text.toString().trim()
                    val roommatePhone = roommateView.findViewById<EditText>(R.id.roommatePhone).text.toString().trim()
                    val roommateEmail = roommateView.findViewById<EditText>(R.id.roommateEmail).text.toString().trim()

                    if (roommateName.isNotEmpty() && roommatePhone.isNotEmpty() && roommateEmail.isNotEmpty()) {
                        val roommate = User(
                            email = roommateEmail,
                            password = sharedPassword,
                            name = roommateName,
                            phone = roommatePhone,
                            apartmentId = ref.id
                        )
                        roommates.add(
                            mapOf(
                                "name" to roommateName,
                                "phone" to roommatePhone,
                                "email" to roommateEmail
                            )
                        )

                        // Create a new user for each roommate with the same shared password
                        authViewModel.register(roommate).observe(viewLifecycleOwner, Observer { result ->
                            if (result.isSuccess) {
                                // Roommate successfully registered
                                firestore.collection("apartments")
                                    .document(ref.id).update("roommates",roommates)
                                if (i == roommatesContainer.childCount-1){
                                    Toast.makeText(requireContext(), "Roommates registered successfully.", Toast.LENGTH_SHORT).show()
                                    findNavController().navigate(R.id.apartmentManagementFragment)
                                }
                            } else {
                                Toast.makeText(requireContext(), "Failed to register roommate: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
//                Handler(Looper.getMainLooper()).postDelayed({
//                findNavController().navigate(R.id.apartmentManagementFragment)
//                },3000)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to register apartment: ${e.message}", Toast.LENGTH_SHORT).show()
            }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val place = Autocomplete.getPlaceFromIntent(data)
                addressEditText.setText(place.name)
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR && data != null) {
                val status = Autocomplete.getStatusFromIntent(data)
                Toast.makeText(requireContext(), "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Autocomplete canceled or data is null", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
