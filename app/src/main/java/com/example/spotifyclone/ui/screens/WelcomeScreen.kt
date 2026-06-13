package com.example.spotifyclone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.spotifyclone.navigation.Screen

/**
 * WelcomeScreen: Primera pantalla que ve el usuario.
 * Proporciona el punto de entrada para el registro y el inicio de sesión.
 */
@Composable
fun WelcomeScreen(navController: NavHostController) {
    // Contenedor principal con fondo negro sólido
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Temporal (Simulando Spotify)
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = "Logo",
                tint = Color(0xFF1DB954),
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Mensaje de bienvenida
            Text(
                text = "Millones de canciones.\nGratis en Spotify.",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // Botón de Registro Principal (Verde Spotify)
            Button(
                onClick = { navController.navigate(Screen.Register.route) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
            ) {
                Text("Regístrate gratis", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón de Login (Estilo contorno blanco)
            OutlinedButton(
                onClick = { navController.navigate(Screen.LoginOptions.route) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 0.5.dp)
            ) {
                Text("Inicia sesión", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
