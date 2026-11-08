package com.solu8i.cmms.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object TokenProvider {
    var token: String? = null
}

object RetrofitInstance {

    private var baseUrl: String = "https://rumat.ft-tegal.com/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = TokenProvider.token
        val request = if (!token.isNullOrEmpty()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .build()
        } else {
            original.newBuilder()
                .header("Accept", "application/json")
                .build()
        }
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    var api: ApiService = buildApi(baseUrl)
        private set

    fun updateBaseUrl(newUrl: String) {
        val url = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        baseUrl = url
        api = buildApi(baseUrl)
    }

    fun getBaseUrl(): String = baseUrl

    fun getStorageUrl(): String = baseUrl.replace("/api/", "/storage/")

    private fun buildApi(url: String): ApiService {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
