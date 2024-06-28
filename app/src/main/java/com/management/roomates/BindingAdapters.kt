package com.management.roomates

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@BindingAdapter("text")
fun setText(view: TextView, value: String?) {
    view.text = value ?: ""
}

@BindingAdapter("android:text")
fun setText(editText: EditText, value: Int?) {
    val text = value?.toString() ?: ""
    if (editText.text.toString() != text) {
        editText.setText(text)
    }
}

@InverseBindingAdapter(attribute = "android:text")
fun getText(editText: EditText): Int {
    return editText.text.toString().toIntOrNull() ?: 0
}

@BindingAdapter("android:textAttrChanged")
fun setTextWatcher(editText: EditText, textAttrChanged: InverseBindingListener) {
    editText.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {}
        override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
            textAttrChanged.onChange()
        }
        override fun afterTextChanged(editable: Editable) {}
    })
}

@BindingAdapter("timestampToDateTime")
fun setTimestampToDateTime(view: TextView, timestamp: Long) {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val date = Date(timestamp)
    val formattedDate = sdf.format(date)
    view.text = formattedDate
}

@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String?) {
    if (!url.isNullOrEmpty()) {
        Picasso.get().load(url).into(view)
    } else {
        view.setImageResource(R.drawable.placeholder)
    }
}
