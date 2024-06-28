package com.management.roomates.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.management.roomates.data.AppDatabase
import com.management.roomates.data.model.Post
import com.management.roomates.repository.PostRepository
import kotlinx.coroutines.launch

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository
    val allPosts: LiveData<List<Post>>

    init {
        val postDao = AppDatabase.getDatabase(application).postDao()
        repository = PostRepository(postDao)
        allPosts = repository.allPosts
    }

    fun insert(post: Post) = viewModelScope.launch {
        repository.insert(post)
    }

    fun delete(post: Post) = viewModelScope.launch {
        repository.delete(post)
    }

    override fun onCleared() {
        super.onCleared()
        repository.cleanup()
    }
}
