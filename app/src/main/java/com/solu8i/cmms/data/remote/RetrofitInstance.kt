package com.solu8i.cmms.data.remote

import com.solu8i.cmms.BuildConfig
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
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
        level = if (BuildConfig.LOG_HTTP_BODY) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
        redactHeader("Authorization")
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
        require(isAllowedUrl(newUrl)) {
            "URL server harus menggunakan HTTPS pada build release."
        }
        baseUrl = normalizeBaseUrl(newUrl)
        api = buildApi(baseUrl)
    }

    /**
     * Normalisasi base URL agar selalu berakhiran `/api/`.
     * Contoh: `http://192.168.110.112:8000` -> `http://192.168.110.112:8000/api/`
     */
    fun normalizeBaseUrl(url: String): String {
        val parsed = url.trim().toHttpUrlOrNull()
            ?: return if (url.trim().endsWith("/")) url.trim() else "${url.trim()}/"

        val pathSegments = parsed.pathSegments.filter { it.isNotEmpty() }

        if (pathSegments.isEmpty()) {
            return parsed.newBuilder().addPathSegment("api").addPathSegment("").build().toString()
        }

        return parsed.newBuilder()
            .encodedPath("/" + pathSegments.joinToString("/") + "/")
            .build()
            .toString()
    }

    /**
     * Build release hanya mengizinkan HTTPS; build debug boleh HTTP untuk server lokal.
     */
    fun isAllowedUrl(url: String): Boolean {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) {
            return false
        }
        return BuildConfig.ALLOW_CLEARTEXT || trimmed.startsWith("https://", ignoreCase = true)
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
