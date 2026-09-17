package com.example.mentora

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit


data class TutorRequest(val message: String)

data class TutorStep(val speech: String, val audio: String?)

interface MentoraApiService {
    @POST("tutor")
    suspend fun getLesson(@Body request: TutorRequest): List<TutorStep>
}

object RetrofitClient {

    private const val BASE_URL = "http://127.0.0.1:8080/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    val api: MentoraApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MentoraApiService::class.java)
    }
}