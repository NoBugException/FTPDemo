package com.luxshare.common

import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel


abstract class ScopedFragment: Fragment(), CoroutineScope by MainScope(){

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}