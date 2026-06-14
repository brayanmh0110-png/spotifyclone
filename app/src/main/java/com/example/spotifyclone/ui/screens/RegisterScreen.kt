package com.example.spotifyclone.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.spotifyclone.viewmodel.AuthViewModel

/**
 * RegisterScreen: Pantalla de creación de cuenta nueva.
 * Recopila nombre, correo y contraseña para registrar al usuario en Firebase.
 */
@Composable
fun RegisterScreen(
    navController: NavHostController, 
    authViewModel: AuthViewModel
) {
    val estadoUsuario by authViewModel.userState.collectAsState()
    val errorServidor by authViewModel.error.collectAsState()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
            }
        }
    ) { rellenos ->
        Column(
            modifier = Modifier.fillMaxSize().padding(rellenos).padding(24.dp)
        ) {
            Text("Crear cuenta", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))

            // Campo: Nombre Completo
            OutlinedTextField(
                value = estadoUsuario.name, onValueChange = { authViewModel.onNameChange(it) },
                label = { Text("¿Cómo te llamas?") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(Modifier.height(16.dp))

            // Campo: Correo
            OutlinedTextField(
                value = estadoUsuario.email, onValueChange = { authViewModel.onEmailChange(it) },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(Modifier.height(16.dp))

            // Campo: Contraseña (con ocultamiento de caracteres)
            var password by remember { mutableStateOf("") }
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Elige una contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            // Mensaje de error si falla Firebase
            if (errorServidor != null) {
                Text(text = errorServidor!!, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { authViewModel.register(password) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
            ) {
                Text("Registrarme", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
