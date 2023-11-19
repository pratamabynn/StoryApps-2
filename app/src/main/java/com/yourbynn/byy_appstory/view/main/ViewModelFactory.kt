package com.yourbynn.byy_appstory.view.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yourbynn.byy_appstory.data.pref.UserRepository
import com.yourbynn.byy_appstory.di.Injection


class ViewModelFactory(private val repository: UserRepository) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when {
                modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                    MainViewModel(repository) as T
                }
                else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
            }
        }

        companion object {
            @Volatile
            private var INSTANCE: ViewModelFactory? = null
            @JvmStatic
            fun getInstance(context: Context): ViewModelFactory {
                if (INSTANCE == null) {
                    synchronized(ViewModelFactory::class.java) {
                        INSTANCE = ViewModelFactory(
                            Injection.provideRepository(context)
                        )
                    }
                }
                return INSTANCE as ViewModelFactory
            }
        }
}