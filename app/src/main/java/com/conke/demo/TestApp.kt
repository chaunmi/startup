package com.conke.demo

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.cnoke.startup.application.IApplication

class TestApp private constructor(): IApplication {

    /**
     * 必须用此方法实现单例。否则工程会报错
     */
    companion object {
        val instance: TestApp by lazy {
            TestApp()
        }
        const val TAG = "TestApp"
    }

    override fun attachBaseContext(context: Context) {
        Log.e(TAG,"attachBaseContext")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.e(TAG,"onConfigurationChanged")
    }

    override fun onCreate() {
        Log.e(TAG,"onCreate")
    }

    override fun onLowMemory() {
        Log.e(TAG,"onLowMemory")
    }

    override fun onTerminate() {
        Log.e(TAG,"onTerminate")
    }

    override fun onTrimMemory(level: Int) {
        Log.e(TAG,"onTrimMemory")
    }
}