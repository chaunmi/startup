package com.cnoke.test1

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.chaunmi.startup.annotation.StartupInitApplication
import com.cnoke.startup.application.IApplication

/**
 * @date on 2021/12/31
 * @author huanghui
 * @title
 * @describe
 */
@StartupInitApplication
class Test1 private constructor(): IApplication{

    /**
     * 必须用此方法实现单例。否则工程会报错
     */
    companion object {
        val instance: Test1 by lazy {
            Test1()
        }
        const val TAG = "Test1"
    }

    override fun attachBaseContext(context: Context) {
        Log.e(TAG,"attachBaseContext")
    }

    override fun onCreate() {
        Log.e(TAG,"onCreate")
    }

    override fun onTerminate() {
        Log.e(TAG,"onTerminate")
    }

    override fun onLowMemory() {
        Log.e(TAG,"onLowMemory")
    }

    override fun onTrimMemory(level: Int) {
        Log.e(TAG,"onTrimMemory")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.e(TAG,"onConfigurationChanged")
    }

}