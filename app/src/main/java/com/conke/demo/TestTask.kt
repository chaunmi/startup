package com.conke.demo

import android.app.Application
import android.util.Log
import com.cnoke.startup.task.InitTask

class TestTask1 : InitTask {
    override suspend fun execute(application: Application) {
       Log.i(TestApp.TAG, " execute ${name()}")
    }

    override fun name(): String {
       return "TestTask1"
    }
}

class TestTask2 : InitTask {

    override fun anchor(): Boolean {
        return true
    }

    override suspend fun execute(application: Application) {
        Log.i(TestApp.TAG, " execute ${name()}")
    }

    override fun name(): String {
        return "TestTask2"
    }
}

class TestTask3 : InitTask {
    override suspend fun execute(application: Application) {
        Log.i(TestApp.TAG, " execute ${name()}")
    }

    override fun name(): String {
        return "TestTask3"
    }
}