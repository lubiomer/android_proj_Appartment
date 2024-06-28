package com.management.roomates.view

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.storage.FirebaseStorage
import com.management.roomates.data.model.Post
import com.management.roomates.databinding.FragmentPostBinding
import com.management.roomates.viewmodel.PostViewModel
import java.io.ByteArrayOutputStream
import java.io.IOException

class PostFragment : Fragment() {
    private var _binding: FragmentPostBinding? = null
    private val binding get() = _binding!!
    private val postViewModel: PostViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter

    private val storage = FirebaseStorage.getInstance()
    private var imageUri: Uri? = null

    companion object {
        const val PICK_IMAGE_REQUEST = 71
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postAdapter = PostAdapter()
        binding.recyclerViewPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }

        postViewModel.allPosts.observe(viewLifecycleOwner, Observer { posts ->
            postAdapter.submitList(posts)
        })

        binding.buttonChooseImage.setOnClickListener { chooseImage() }
        binding.buttonUpload.setOnClickListener { uploadPost() }
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    private fun uploadPost() {
        val text = binding.editTextPostText.text.toString().trim()
        if (text.isEmpty() || imageUri == null) {
            Toast.makeText(requireContext(), "Please enter text and choose an image", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = storage.reference.child("postImages/${System.currentTimeMillis()}")
        ref.putFile(imageUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    val post = Post(
                        userId = "dummyUserId",
                        text = text,
                        imageUrl = uri.toString(),
                        timestamp = System.currentTimeMillis()
                    )
                    postViewModel.insert(post)
                    binding.editTextPostText.text?.clear()
                    imageUri = null
                    binding.imageViewPreview.setImageBitmap(null)
                    Toast.makeText(requireContext(), "Post uploaded successfully", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            try {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
                binding.imageViewPreview.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
