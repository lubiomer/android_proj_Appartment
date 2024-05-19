package com.android.roommates

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.roommates.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.existingUser.setOnClickListener {
            Toast.makeText(this, "EXISTING USER", Toast.LENGTH_SHORT).show()
        }
        binding.newApartment.setOnClickListener {
            startActivity(Intent(this, NewApartmentActivity::class.java))
        }
    }
}