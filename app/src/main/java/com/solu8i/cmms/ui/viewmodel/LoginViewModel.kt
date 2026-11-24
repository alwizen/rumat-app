package com.solu8i.cmms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solu8i.cmms.data.model.LoginRequest
import com.solu8i.cmms.data.remote.RetrofitInstance
import com.solu8i.cmms.data.store.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val isSessionLoaded: Boolean = false,
    val userRoles: List<String> = emptyList(),
    val baseUrl: String = RetrofitInstance.getBaseUrl(),
    val isTestingConnection: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.IDLE,
    val connectionError: String? = null
)

enum class ConnectionStatus {
    IDLE, SUCCESS, FAILED
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    val tokenManager = TokenManager(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    init {
        checkExistingToken()
    }

    private fun checkExistingToken() {
        viewModelScope.launch {
            val token = tokenManager.token.firstOrNull()
            if (!token.isNullOrEmpty()) {
                com.solu8i.cmms.data.remote.TokenProvider.token = token
                val roles = tokenManager.userRoles.firstOrNull()
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = true,
                    isSessionLoaded = true,
                    userRoles = parseRoles(roles)
                )
            } else {
                _uiState.value = _uiState.value.copy(isSessionLoaded = true)
            }
        }
    }

    private fun parseRoles(roles: String?): List<String> {
        return roles
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()
    }

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun onBaseUrlChanged(url: String) {
        _uiState.value = _uiState.value.copy(
            baseUrl = url,
            connectionStatus = ConnectionStatus.IDLE
        )
    }

    fun applyBaseUrl(): Boolean {
        val url = _uiState.value.baseUrl.trim()
        if (url.isBlank()) {
            return true
        }

        if (!RetrofitInstance.isAllowedUrl(url)) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "URL server harus menggunakan HTTPS."
            )
            return false
        }

        RetrofitInstance.updateBaseUrl(url)
        _uiState.value = _uiState.value.copy(baseUrl = RetrofitInstance.getBaseUrl())
        return true
    }

    fun testConnection() {
        val url = _uiState.value.baseUrl.trim()
        if (url.isBlank()) {
            _uiState.value = _uiState.value.copy(
                connectionStatus = ConnectionStatus.FAILED,
                connectionError = "URL server harus diisi."
            )
            return
        }

        if (!RetrofitInstance.isAllowedUrl(url)) {
            _uiState.value = _uiState.value.copy(
                connectionStatus = ConnectionStatus.FAILED,
                connectionError = "URL server harus menggunakan HTTPS."
            )
            return
        }

        val normalized = RetrofitInstance.normalizeBaseUrl(url)
        RetrofitInstance.updateBaseUrl(normalized)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                baseUrl = normalized,
                isTestingConnection = true,
                connectionStatus = ConnectionStatus.IDLE,
                connectionError = null
            )

            try {
                val (probe, housekeepingProbe) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val client = okhttp3.OkHttpClient.Builder()
                        .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                        .build()

                    fun probeUrl(path: String): Int {
                        val request = okhttp3.Request.Builder()
                            .url(normalized + path)
                            .header("Accept", "application/json")
                            .get()
                            .build()

                        return client.newCall(request).execute().use { response -> response.code }
                    }

                    probeUrl("me") to probeUrl("housekeeping-plans")
                }

                when (probe) {
                    in 200..399, 401, 403, 500 -> {
                        if (housekeepingProbe == 404) {
                            _uiState.value = _uiState.value.copy(
                                isTestingConnection = false,
                                connectionStatus = ConnectionStatus.FAILED,
                                connectionError = "Server terhubung, tetapi endpoint housekeeping belum tersedia (404). " +
                                    "Pastikan server sudah di-deploy dengan modul housekeeping dan cache route sudah di-refresh."
                            )
                        } else {
                            connectionSuccess()
                        }
                    }
                    404 -> _uiState.value = _uiState.value.copy(
                        isTestingConnection = false,
                        connectionStatus = ConnectionStatus.FAILED,
                        connectionError = "Endpoint API tidak ditemukan. Pastikan URL berakhiran /api/."
                    )
                    else -> _uiState.value = _uiState.value.copy(
                        isTestingConnection = false,
                        connectionStatus = ConnectionStatus.FAILED,
                        connectionError = "Server merespons dengan status $probe. Periksa URL server."
                    )
                }
            } catch (e: java.net.UnknownHostException) {
                _uiState.value = _uiState.value.copy(
                    isTestingConnection = false,
                    connectionStatus = ConnectionStatus.FAILED,
                    connectionError = "Host tidak ditemukan. Periksa URL."
                )
            } catch (e: java.net.ConnectException) {
                _uiState.value = _uiState.value.copy(
                    isTestingConnection = false,
                    connectionStatus = ConnectionStatus.FAILED,
                    connectionError = "Tidak bisa koneksi ke server. Pastikan server aktif."
                )
            } catch (e: java.net.SocketTimeoutException) {
                _uiState.value = _uiState.value.copy(
                    isTestingConnection = false,
                    connectionStatus = ConnectionStatus.FAILED,
                    connectionError = "Koneksi timeout. Periksa jaringan."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isTestingConnection = false,
                    connectionStatus = ConnectionStatus.FAILED,
                    connectionError = "${e.javaClass.simpleName}: ${e.message}"
                )
            }
        }
    }

    private fun connectionSuccess() {
        _uiState.value = _uiState.value.copy(
            isTestingConnection = false,
            connectionStatus = ConnectionStatus.SUCCESS,
            connectionError = null
        )
    }

    fun login() {
        val state = _uiState.value

        if (state.email.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Email harus diisi")
            return
        }

        if (state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Password harus diisi")
            return
        }

        if (!applyBaseUrl()) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val response = RetrofitInstance.api.login(
                    LoginRequest(email = state.email, password = state.password)
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success && body.data != null) {
                        com.solu8i.cmms.data.remote.TokenProvider.token = body.data.token
                        tokenManager.saveAuthData(
                            token = body.data.token,
                            name = body.data.user.name,
                            email = body.data.user.email,
                            roles = body.data.user.roles
                        )
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            userRoles = body.data.user.roles
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = body?.message ?: "Login gagal"
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = if (errorBody != null) {
                        try {
                            val json = com.google.gson.Gson().fromJson(
                                errorBody,
                                com.solu8i.cmms.data.model.LoginResponse::class.java
                            )
                            json.message
                        } catch (e: Exception) {
                            "Email atau password salah"
                        }
                    } else {
                        "Terjadi kesalahan. Coba lagi."
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = message
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Tidak dapat terhubung ke server.\n${RetrofitInstance.getBaseUrl()}"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.logout()
            } catch (_: Exception) {
            }

            com.solu8i.cmms.data.remote.TokenProvider.token = null
            tokenManager.clearAuthData()
            _uiState.value = LoginUiState(
                baseUrl = _uiState.value.baseUrl,
                isSessionLoaded = true
            )
        }
    }
}
