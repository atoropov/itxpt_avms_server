package com.example.itxptavmsserver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

inline fun viewModelFactory(crossinline f: () -> ViewModel) =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return f() as T
            }
        }
