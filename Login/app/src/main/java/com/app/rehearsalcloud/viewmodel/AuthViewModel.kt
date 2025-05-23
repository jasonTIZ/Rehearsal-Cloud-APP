package com.app.rehearsalcloud.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rehearsalcloud.model.User
import com.app.rehearsalcloud.repository.AuthRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {





    var users by mutableStateOf<List<User>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")
        private set

    var isAuthenticated by mutableStateOf(false)
        private set

    var currentUser by mutableStateOf<User?>(null)
        private set

    // Función para limpiar el estado de autenticación
    fun clearAuthStatus() {
        var authStatus = null
    }



    // Register a new user
    fun registerUser(username: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""

            try {
                val newUser = User(
                    username = username,
                    email = email,
                    password = password
                )

                val registeredUser = repository.registerUser(newUser)
                currentUser = registeredUser
                isAuthenticated = true

                // Llama a la función de éxito que viene desde el Composable
                onSuccess()

            } catch (e: Exception) {
                errorMessage = when {
                    e.message?.contains("exitosamente") == true -> {
                        "¡Registro exitoso! Por favor inicia sesión"
                    }
                    e.message?.contains("ya está en uso") == true -> {
                        "El nombre de usuario o correo ya está registrado"
                    }
                    e.message?.contains("al menos 8 caracteres") == true -> {
                        "La contraseña debe tener al menos 8 caracteres"
                    }
                    else -> {
                        Log.e("AUTH", "Registration error", e)
                        "Error en el registro: ${e.message ?: "por favor intenta nuevamente"}"
                    }
                }
            } finally {
                isLoading = false
            }
        }
    }
    // Login user
    fun loginUser(username: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""

            try {
                val user = User(
                    username = username,
                    password = password,
                    email = "" // No necesario para login
                )

                val loginSuccess = repository.loginUser(user)
                if (loginSuccess) {
                    isAuthenticated = true
                    // Opcional: guardar datos básicos del usuario
                    currentUser = User(
                        id = null, // Obtenerlo después si es necesario
                        username = username,
                        email = "",
                        password = "" // No almacenar la contraseña
                    )
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error desconocido en el login"
                Log.e("AUTH", "Login error", e)
            } finally {
                isLoading = false
            }
        }
    }

    // Logout user
    fun logout() {
        currentUser = null
        isAuthenticated = false
        Log.d("AUTH", "User logged out")
    }

    // Load all users (for admin purposes)
    fun loadUsers() {
        viewModelScope.launch {
            isLoading = true
            Log.d("AUTH", "Loading users...")
            try {
                users = repository.getUsers()
                Log.d("AUTH", "Users loaded: ${users.size} items")
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load users"
                Log.e("AUTH", "Failed to load users: ${e.localizedMessage}", e)
            } finally {
                isLoading = false
            }
        }
    }

    // Delete a user (for admin purposes)
    fun deleteUser(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteUser(id)
                users = users.filter { it.id != id }
                Log.d("AUTH", "User with ID $id deleted successfully")

                // If the deleted user is the current user, log them out
                if (currentUser?.id == id) {
                    logout()
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to delete user"
                Log.e("AUTH", "Failed to delete user: ${e.localizedMessage}", e)
            }
        }
    }

    // Clear error message
    fun clearErrorMessage() {
        errorMessage = ""
    }
}