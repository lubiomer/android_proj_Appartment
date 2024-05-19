package com.android.roommates

import android.graphics.Rect
import android.view.MotionEvent
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber
import kotlin.math.abs

/**
 * @author Perry Lance
 * @since 2024-05-10 Created
 */
abstract class BaseActivity : AppCompatActivity() {

    private var actionDownX = 0f
    private var actionDownY = 0f


    open fun enableBlankTouch() = true

    /**
     * Click on the blank space to close the soft keyboard and make the EditText lose focus.
     */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (enableBlankTouch()) {
            val x = event.x
            val y = event.y
            if (event.action == MotionEvent.ACTION_DOWN) {
                actionDownX = x
                actionDownY = y
            } else if (event.action == MotionEvent.ACTION_UP && !(abs(x - actionDownX) > 50 || abs(y - actionDownY) > 50)) {
                val v = currentFocus
                if (v is EditText) {
                    val outRect = Rect()
                    v.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        Timber.v("Hide keyboard")
                        KeyboardUtils.hideKeyboard(this)
                        v.isFocusable = false
                        v.isFocusableInTouchMode = true
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}