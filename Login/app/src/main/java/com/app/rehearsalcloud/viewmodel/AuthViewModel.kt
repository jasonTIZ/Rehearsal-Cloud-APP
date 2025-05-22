package com.example.rehearsalcloud.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rehearsalcloud.models.CreateUserRequest
import com.example.rehearsalcloud.models.DeleteUserResponse
import com.example.rehearsalcloud.models.LoginRequest
import com.example.rehearsalcloud.models.User
import com.example.rehearsalcloud.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> get() = _users

    private val _authStatus = MutableStateFlow<String>("")
    val authStatus: StateFlow<String> get() = _authStatus

    private val _deleteStatus = MutableStateFlow<String>("")
    val deleteStatus: StateFlow<String> get() = _deleteStatus

    // Estado de carga para indicar cuando una operación está en progreso
    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    fun registerUser(username: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.registerUser(
                    CreateUserRequest(username, email, password)
                )
                _authStatus.value = response
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error registering user", e)
                _authStatus.value = "Error al registrar el usuario: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginUser(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.loginUser(LoginRequest(username, password))
                _authStatus.value = response
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error logging in", e)
                _authStatus.value = "Error al iniciar sesión: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchUsers() {
        viewModelScope.launch {
            try {
                val usersList = RetrofitInstance.api.getUsers()
                _users.value = usersList
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching users", e)
            }
        }
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            try {
                val response: DeleteUserResponse = RetrofitInstance.api.deleteUser(userId)
                _deleteStatus.value = response.message
                fetchUsers() // Actualiza la lista después de eliminar
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error deleting user", e)
                _deleteStatus.value = "Error al eliminar el usuario: ${e.localizedMessage}"
            }
        }
    }

    // Método para limpiar el estado de autenticación
    fun clearAuthStatus() {
        _authStatus.value = ""
    }
}