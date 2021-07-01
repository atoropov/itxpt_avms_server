package com.example.itxptavmsserver

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders

class MainActivity : AppCompatActivity() {

    private val avmsViewModel: AvmsServiceViewModel by lazy {
        val factory = viewModelFactory { AvmsServiceViewModel(application) }
        ViewModelProviders.of(this, factory).get(AvmsServiceViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        avmsViewModel.registerService()
    }
}
