package com.yourbynn.byy_appstory.di

import android.content.Context
import com.yourbynn.byy_appstory.config.ApiConfig
import com.yourbynn.byy_appstory.data.pref.UserPreference
import com.yourbynn.byy_appstory.data.pref.UserRepository
import com.yourbynn.byy_appstory.data.pref.dataStore

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService()
        return UserRepository.getInstance(apiService, pref)
    }
}