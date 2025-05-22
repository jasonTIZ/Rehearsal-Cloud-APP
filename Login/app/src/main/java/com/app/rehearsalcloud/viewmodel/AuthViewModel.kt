package com.app.rehearsalcloud.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rehearsalcloud.model.User
import com.app.rehearsalcloud.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    var currentUser by mutableStateOf<User?>(null)
        private set

    var users by mutableStateOf<List<User>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isAuthenticated by mutableStateOf(false)
        private set

    // Register a new user
    fun registerUser(username: String, email: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            Log.d("AUTH", "Registering user...")
            try {
                val newUser = User(
                    username = username,
                    email = email,
                    password = password
                )
                val registeredUser = repository.registerUser(newUser)
                currentUser = registeredUser
                isAuthenticated = true
                Log.d("AUTH", "User registered successfully: ${registeredUser.username}")
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unknown error occurred during registration"
                Log.e("AUTH", "Failed to register user: ${e.localizedMessage}", e)
            } finally {
                isLoading = false
            }
        }
    }

    // Login user
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            Log.d("AUTH", "Logging in user...")
            try {
                val user = User(
                    email = email,
                    password = password,
                    username = "" // Username not needed for login
                )
                val loggedInUser = repository.loginUser(user)
                currentUser = loggedInUser
                isAuthenticated = true
                Log.d("AUTH", "User logged in successfully: ${loggedInUser.email}")
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unknown error occurred during login"
                Log.e("AUTH", "Failed to login user: ${e.localizedMessage}", e)
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
        errorMessage = null
    }
}