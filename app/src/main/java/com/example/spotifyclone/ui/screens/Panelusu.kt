package com.example.spotifyclone.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.spotifyclone.navigation.Screen
import com.example.spotifyclone.viewmodel.AuthViewModel

/**
 * PanelUsuScreen: Menú lateral que permite gestionar la cuenta.
 * Es un Diálogo que aparece desde la izquierda simulando el panel de Spotify.
 */
@Composable
fun PanelUsuScreen(
    navController: NavHostController, 
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val estadoUsuario by authViewModel.userState.collectAsState()

    var mostrarDialogoNombre by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }

    // Lanzador para abrir la galería del teléfono y elegir foto de perfil
    val lanzadorSelectorFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { authViewModel.updateProfilePicture(it.toString()) }
        }
    )

    // Fondo oscuro semi-transparente que cierra el panel si tocas fuera
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)).clickable { navController.popBackStack() }
    ) {
        // --- CUERPO DEL PANEL (Ocupa el 82% del ancho) ---
        Column(
            modifier = Modifier.fillMaxHeight().fillMaxWidth(0.82f).background(Color.Black).clickable(enabled = false) {}.padding(16.dp)
        ) {
            // Cabecera: Perfil e información básica
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { 
                    nuevoNombre = estadoUsuario.name
                    mostrarDialogoNombre = true 
                }
            ) {
                if (estadoUsuario.photoUrl.isNotEmpty()) {
                    AsyncImage(model = estadoUsuario.photoUrl, null, modifier = Modifier.size(50.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.AccountCircle, null, Modifier.size(50.dp), tint = Color.Gray)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    val nombreAMostrar = if (estadoUsuario.name.isNotEmpty()) estadoUsuario.name else estadoUsuario.email.substringBefore("@")
                    Text(nombreAMostrar, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Ver perfil", color = Color.Gray, fontSize = 12.sp)
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.White.copy(alpha = 0.2f))

            // --- LISTA DE OPCIONES ---
            OptionItem(Icons.Default.Image, "Cambiar foto de perfil", alPulsar = {
                lanzadorSelectorFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            })
            OptionItem(Icons.Default.History, "Recientes", alPulsar = { navController.navigate(Screen.ActivityLog.route) })
            OptionItem(Icons.Default.Settings, "Configuración y privacidad")
            OptionItem(Icons.AutoMirrored.Filled.Logout, "Cerrar sesión", alPulsar = { authViewModel.logout() })
            OptionItem(Icons.Default.DeleteForever, "Eliminar cuenta", alPulsar = { authViewModel.deleteAccount() })
        }

        // Diálogo para editar el nombre de usuario
        if (mostrarDialogoNombre) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoNombre = false },
                title = { Text("Editar nombre", color = Color.White) },
                text = {
                    OutlinedTextField(
                        value = nuevoNombre, onValueChange = { nuevoNombre = it },
                        singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                },
                confirmButton = {
                    TextButton(onClick = { if (nuevoNombre.isNotBlank()) { authViewModel.updateName(nuevoNombre); mostrarDialogoNombre = false } }) {
                        Text("Guardar", color = Color(0xFF1DB954))
                    }
                },
                containerColor = Color(0xFF282828)
            )
        }
    }
}

/**
 * OptionItem: Fila reutilizable para las opciones del menú.
 */
@Composable
fun OptionItem(icono: ImageVector, titulo: String, alPulsar: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { alPulsar() }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, null, tint = Color.White, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = titulo, color = Color.White, fontSize = 16.sp)
    }
}
