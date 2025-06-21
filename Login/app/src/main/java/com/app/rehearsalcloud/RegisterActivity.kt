package com.app.rehearsalcloud

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.app.rehearsalcloud.ui.theme.EventFinderTheme
import com.app.rehearsalcloud.viewmodel.AuthViewModel
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

class RegisterActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EventFinderTheme {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showEmptyFieldsToast by remember { mutableStateOf(false) }

    // Estados del ViewModel
    val isLoading = authViewModel.isLoading
    val errorMessage = authViewModel.errorMessage
    val isAuthenticated = authViewModel.isAuthenticated

    val context = LocalContext.current

    // Efecto para mostrar Toast de campos vacíos
    if (showEmptyFieldsToast) {
        LaunchedEffect(showEmptyFieldsToast) {
            Toast.makeText(
                context,
                "Por favor completa todos los campos",
                Toast.LENGTH_SHORT
            ).show()
            showEmptyFieldsToast = false
        }
    }

    // Efecto para manejar registro exitoso
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            Toast.makeText(
                context,
                "¡Registro exitoso! Por favor inicia sesión",
                Toast.LENGTH_SHORT
            ).show()
            onNavigateToLogin()
        }
    }

    // Efecto para mostrar mensajes de error
    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            authViewModel.clearErrorMessage()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        )
            {
            Image(
                painter = painterResource(id = R.drawable.logo_cloud),
                contentDescription = "Logo de Rehearsal Cloud",
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 8.dp)
            )
            Text(
                text = "Rehearsal Cloud",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Usuario") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility
                            else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ocultar contraseña"
                            else "Mostrar contraseña"
                        )
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (username.isBlank() || email.isBlank() || password.isBlank()) {
                        showEmptyFieldsToast = true
                    } else {
                        authViewModel.registerUser(
                            username = username,
                            email = email,
                            password = password,
                            onSuccess = onNavigateToLogin
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Registrarse")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("¿Ya tienes cuenta? Inicia sesión", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}