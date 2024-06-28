package com.management.roomates.repository

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.management.roomates.data.dao.PostDao
import com.management.roomates.data.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostRepository(private val postDao: PostDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private var firestoreListener: ListenerRegistration? = null

    val allPosts: LiveData<List<Post>> = postDao.getAllPosts()

    init {
        firestoreListener = firestore.collection("posts")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                snapshots?.documentChanges?.forEach { documentChange ->
                    val post = documentChange.document.toObject(Post::class.java)
                    when (documentChange.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED -> {
                            insertToRoom(post)
                        }
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                            insertToRoom(post)
                        }
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                            deleteFromRoom(post)
                        }
                    }
                }
            }
    }

    private fun insertToRoom(post: Post) {
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            postDao.insertPost(post)
        }
    }

    private fun deleteFromRoom(post: Post) {
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            postDao.deletePost(post)
        }
    }

    suspend fun insert(post: Post) {
        withContext(Dispatchers.IO) {
            postDao.insertPost(post)
            firestore.collection("posts").document(post.id.toString()).set(post)
        }
    }

    suspend fun delete(post: Post) {
        withContext(Dispatchers.IO) {
            postDao.deletePost(post)
            firestore.collection("posts").document(post.id.toString()).delete()
        }
    }

    fun cleanup() {
        firestoreListener?.remove()
    }
}
