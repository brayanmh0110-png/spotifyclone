package com.example.spotifyclone.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.spotifyclone.viewmodel.AuthViewModel

/**
 * LoginEmailScreen: Permite al usuario entrar a su cuenta usando su correo.
 */
@Composable
fun LoginEmailScreen(
    controladorNavegacion: NavHostController, 
    vistaModeloAutenticacion: AuthViewModel
) {
    val estadoUsuario by vistaModeloAutenticacion.userState.collectAsState()
    var contrasena by remember { mutableStateOf("") }
    val errorServidor by vistaModeloAutenticacion.error.collectAsState()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            IconButton(onClick = { controladorNavegacion.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
            }
        }
    ) { rellenos ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(rellenos)
                .padding(24.dp)
        ) {
            Text("Inicia sesión", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = estadoUsuario.email,
                onValueChange = { vistaModeloAutenticacion.onEmailChange(it) },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            if (errorServidor != null) {
                Text(text = errorServidor!!, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { vistaModeloAutenticacion.login(contrasena) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
            ) {
                Text("Iniciar sesión", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
