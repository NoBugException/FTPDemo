package com.luxshare.common

import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.luxshare.common.tools.KeyboardUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel


abstract class ScopedActivity: AppCompatActivity(), CoroutineScope by MainScope(){

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                // 隐藏软键盘
                KeyboardUtil.hideSoftKeyboard(this@ScopedActivity)
            }
            else -> {}
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}