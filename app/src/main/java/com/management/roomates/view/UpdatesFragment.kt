package com.management.roomates.view

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.management.roomates.BuildConfig
import com.management.roomates.CustomApplication
import com.management.roomates.R
import com.management.roomates.data.model.Update
import com.management.roomates.databinding.FragmentUpdatesBinding
import com.management.roomates.viewmodel.UpdatesViewModel
import com.management.roomates.viewmodel.UpdatesViewModelFactory
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date


class UpdatesFragment : Fragment() {
    private val REQUEST_IMAGE_CAPTURE = 1
    private var _binding: FragmentUpdatesBinding? = null
    private val binding get() = _binding!!
    private lateinit var updatesAdapter: UpdatesAdapter
    private val auth = FirebaseAuth.getInstance()
    private var selectedImageUri: Uri?=null
    private var currentPhotoPath:File? = null
    private val updatesViewModel: UpdatesViewModel by viewModels {
        UpdatesViewModelFactory(CustomApplication.loggedUserId) // currentUserId는 실제로 사용되는 ID로 대체해야 합니다.
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updatesAdapter = UpdatesAdapter(updatesViewModel) { update ->
//            toggleEditCleaningShift(cleaningShift)
        }

        binding.recyclerViewUpdates.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = updatesAdapter
            updatesAdapter.setOnItemClickListener(object :UpdatesAdapter.OnItemClickListener{
                override fun deleteClick(update: Update) {
                    updatesViewModel.delete(update){
                        loadUpdates()
                    }
                }
            })
        }

        loadUpdates()

        binding.buttonUploadImage.setOnClickListener {
            // Check for permissions
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_IMAGE_CAPTURE
                )
            } else {
                // Launch camera intent
                dispatchTakePictureIntent()
            }
        }

        binding.buttonAddUpdate.setOnClickListener {
            val notes = binding.editTextNote.text.toString()
            val lastUpdatedAt = System.currentTimeMillis()
            val uploadedImagePath = ""
            if (notes.isNotEmpty()){
                if (currentPhotoPath != null){
                    binding.buttonAddUpdate.text = "Please wait..."
                    binding.buttonAddUpdate.isEnabled = false
                    CoroutineScope(Dispatchers.Main).launch {
                        val downloadUrl = uploadImageToFirebase(currentPhotoPath!!.toUri())
                        if (downloadUrl != null) {
                            println("Download URL: $downloadUrl")
                            // Use the download URL as needed
                            val update = Update(
                                "",
                                CustomApplication.loggedUserId,
                                "$lastUpdatedAt",
                                notes,
                                downloadUrl.toString(),
                                CustomApplication.loggedUserApartmentId
                            )
                            updatesViewModel.insert(update){
                                binding.editTextNote.text?.clear()
                                Toast.makeText(requireContext(), "Update added successfully!", Toast.LENGTH_SHORT).show()
                                binding.buttonAddUpdate.text = "Add Update"
                                binding.buttonAddUpdate.isEnabled = true
                                currentPhotoPath = null
                                binding.imageview.setImageResource(R.drawable.placeholder)
                                loadUpdates()
                            }
                        } else {
                            binding.buttonAddUpdate.text = "Add Update"
                            binding.buttonAddUpdate.isEnabled = true
                            Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else{
                    val update = Update(
                        "",
                        CustomApplication.loggedUserId,
                        "$lastUpdatedAt",
                        notes,
                        uploadedImagePath,
                        CustomApplication.loggedUserApartmentId
                    )
                    updatesViewModel.insert(update){
                        binding.editTextNote.text?.clear()
                        Toast.makeText(requireContext(), "Update added successfully!", Toast.LENGTH_SHORT).show()
                        binding.buttonAddUpdate.text = "Add Update"
                        binding.buttonAddUpdate.isEnabled = true
                        currentPhotoPath = null
                        binding.imageview.setImageResource(R.drawable.placeholder)
                        loadUpdates()
                    }
                }

            }
            else{
                Toast.makeText(requireContext(), "Please write a note", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun uploadImageToFirebase(fileUri: Uri): Uri? {
        return withContext(Dispatchers.IO) {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val imageRef = storageRef.child("images/${fileUri.lastPathSegment}")

            try {
                imageRef.putFile(fileUri).await()
                val downloadUrl = imageRef.downloadUrl.await()
                return@withContext downloadUrl
            } catch (e: Exception) {
                println("Upload failed: ${e.message}")
                return@withContext null
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(requireActivity(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
           currentPhotoPath = createImageFile()
            val uri = FileProvider.getUriForFile(requireActivity(), "${BuildConfig.APPLICATION_ID}.fileprovider", currentPhotoPath!!)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
            Log.d("TEST1999",e.localizedMessage!!)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
//            currentPhotoPath = absolutePath

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
//            uploadImageToFirebase(photoURI)
            Picasso.get().load(currentPhotoPath!!).into(binding.imageview)
        } else {
            Toast.makeText(requireActivity(), "Image capture failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUpdates(){
        updatesViewModel.loadUpdates(CustomApplication.loggedUserApartmentId).observe(viewLifecycleOwner
        ) { updates ->
            updatesAdapter.submitList(updates)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpdatesBinding.inflate(inflater, container, false).apply {
            viewModel = updatesViewModel
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}