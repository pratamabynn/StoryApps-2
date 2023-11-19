package com.yourbynn.byy_appstory.data.pref

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.google.gson.Gson
import com.yourbynn.byy_appstory.config.ApiService
import com.yourbynn.byy_appstory.data.response.ListStoryItem
import com.yourbynn.byy_appstory.data.response.ResponseAddStories
import com.yourbynn.byy_appstory.data.response.ResponseDetails
import com.yourbynn.byy_appstory.data.response.ResponseInvalid
import com.yourbynn.byy_appstory.data.response.ResponseLogins
import com.yourbynn.byy_appstory.data.response.ResponseStories
import com.yourbynn.byy_appstory.data.response.Story
import com.yourbynn.byy_appstory.view.pagging.StoriesPagging
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.File

class UserRepository private constructor (private val apiService: ApiService, private val preference: UserPreference){

    private val _detail = MutableLiveData<Story>()
    val detail: LiveData<Story> = _detail

    private val _storiesWithLocation = MutableLiveData<List<ListStoryItem>>()
    val storiesWithLocation: LiveData<List<ListStoryItem>> = _storiesWithLocation

    fun register(name: String, email: String, password: String) = liveData {
        emit(ResultValue.Loading)
        try {
            val successResponse = apiService.register(name, email, password)
            val message = successResponse.message
            emit(ResultValue.Success(message))
        } catch (e: HttpException) {
            val errorMessage: String
            if (e.code() == 400) {
                errorMessage = "Email sudah dimasukan"
                emit(ResultValue.Error(errorMessage))
            } else {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, ResponseInvalid::class.java)
                errorMessage = errorBody.message.toString()
                emit(ResultValue.Error(errorMessage))
            }
        }
    }

    fun login(email: String, password: String) = liveData {
        emit(ResultValue.Loading)
        try {
            val successResponse = apiService.login(email, password)
            val data = successResponse.loginResult?.token
            emit(ResultValue.Success(data))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ResponseLogins::class.java)
            emit(ResultValue.Error(errorResponse.message!!))
        }
    }

    fun getStories(token: String): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            pagingSourceFactory = {
                StoriesPagging(apiService,"Bearer $token")
            }
        ).liveData
    }

    fun getDetailStory(token: String, id: String) {
        val client = apiService.detailStory("Bearer $token", id)
        client.enqueue(object : Callback<ResponseDetails> {
            override fun onResponse(call: Call<ResponseDetails>, response: Response<ResponseDetails>
            ) {
                if (response.isSuccessful) {
                    _detail.value = response.body()?.story!!
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseDetails>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }
        })
    }

    fun uploadImage(token: String, imageFile: File, description: String) = liveData {
        emit(ResultValue.Loading)
        val requestBody = description.toRequestBody("text/plain".toMediaType())
        val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData("photo", imageFile.name, requestImageFile)
        try {
            val successResponse = apiService.addStory("Bearer $token", multipartBody, requestBody)
            emit(ResultValue.Success(successResponse))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ResponseAddStories::class.java)
            emit(ResultValue.Error(errorResponse.message))
        }
    }

    fun getStoriesWithLocation(token: String) {
        val client = apiService.getStoriesWithLocation("Bearer $token")
        client.enqueue(object : Callback<ResponseStories> {
            override fun onResponse(call: Call<ResponseStories>, response: Response<ResponseStories>
            ) {
                if (response.isSuccessful) {
                    _storiesWithLocation.value = response.body()?.listStory
                } else {
                    Log.e(TAG, "onFailureLocation: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseStories>, t: Throwable) {
                Log.e(TAG, "onFailureLocation: ${t.message.toString()}")
            }
        })
    }

    fun getSession(): Flow<UserModel> {
        return preference.getSession()
    }

    suspend fun saveSession(user: UserModel) {
        preference.saveSession(user)
    }

    suspend fun logout() {
        preference.logout()
        instance = null
    }

    companion object {
        private const val TAG = "MainViewModel"
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(apiService: ApiService, pref: UserPreference) =
            instance ?: synchronized(this) {
                instance ?: UserRepository(apiService, pref)
            }.also { instance = it }
    }
}