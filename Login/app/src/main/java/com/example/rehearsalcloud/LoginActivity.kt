package com.example.rehearsalcloud

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.rehearsalcloud.ui.theme.EventFinderTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EventFinderTheme {
                LoginScreen(
                    onNavigateToRegister = {
                        val intent = Intent(this, RegisterActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onNavigateToRegister: () -> Unit) {
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
            value = "",
            onValueChange = {},
            label = { Text("Enter your username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = "",
            onValueChange = {},
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { /* Lógica para el inicio de sesión */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Log In")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { onNavigateToRegister() }) {
            Text("¿No tienes una cuenta? Registrate", color = MaterialTheme.colorScheme.primary)
        }
    }
}
