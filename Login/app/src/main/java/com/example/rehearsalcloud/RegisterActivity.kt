package com.example.rehearsalcloud

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.rehearsalcloud.ui.theme.EventFinderTheme

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EventFinderTheme {
                RegisterScreen(
                    onNavigateToLogin = {
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onNavigateToLogin: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Spacer(modifier = Modifier.height(24.dp))
        Text("Rehearsal Cloud", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Enter your username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Enter your email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(if (passwordVisible) "Ocultar" else "Mostrar")
                }
            },
            keyboardOptions = KeyboardOptions.Default,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { /* Lógica para registrar */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Confirmar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { onNavigateToLogin() }) {
            Text("¿Ya tienes una cuenta? Iniciar sesión", color = MaterialTheme.colorScheme.primary)
        }
    }
}
