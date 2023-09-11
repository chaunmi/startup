package com.conke.demo

import android.util.Log
import com.cnoke.startup.application.IApplication

class TestInsertClasses {

    private val applist : MutableList<IApplication> = mutableListOf()

    init {
        init()
    }

    private fun init() {

    }

    fun register(app : IApplication) {
        applist.add(app)
    }

    fun onCreate() {
        applist.forEach {
            Log.i(TestApp.TAG, " TestInsertClasses onCreate ${it.javaClass.simpleName} ")
            it.onCreate()
        }
    }
}