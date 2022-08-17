package com.dorck.android.publish

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dorck.kotlin.library.sample.MyKotlinClass

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MyKotlinClass().doSomething()
    }
}