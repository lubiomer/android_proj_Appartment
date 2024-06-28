package com.management.roomates.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.management.roomates.data.model.Post

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): LiveData<List<Post>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Delete
    suspend fun deletePost(post: Post)
}
