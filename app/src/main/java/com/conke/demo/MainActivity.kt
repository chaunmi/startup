package com.conke.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.cnoke.demo.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e("MainActivity","     onCreate    ")
    }
}