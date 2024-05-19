package com.android.roommates

import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.android.roommates.databinding.ActivityNewApartmentBinding

/**
 * @author Perry Lance
 * @since 2024-05-10 Created
 */
class NewApartmentActivity : BaseActivity() {

    private lateinit var binding: ActivityNewApartmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_apartment)

        binding.finish.setOnClickListener {
            Toast.makeText(this, "FINISH", Toast.LENGTH_SHORT).show()
        }
    }
}