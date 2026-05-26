package com.example.spotifyclone.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.spotifyclone.viewmodel.AuthViewModel

/**
 * PanelUsuScreen: Menú lateral que se muestra como un diálogo.
 * Permite gestionar la cuenta del usuario, cambiar foto y cerrar sesión.
 */
@Composable
fun PanelUsuScreen(
    controladorNavegacion: NavHostController, 
    vistaModeloAutenticacion: AuthViewModel
) {
    // Obtenemos los datos del usuario autenticado
    val estadoUsuario by vistaModeloAutenticacion.userState.collectAsState()
    
    // Herramienta para abrir la galería y elegir una foto
    val lanzadorSelectorFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                // Si el usuario elige una foto, la procesamos en el ViewModel
                vistaModeloAutenticacion.updateProfilePicture(it.toString())
            }
        }
    )

    // Contenedor principal con fondo semi-transparente
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable { controladorNavegacion.popBackStack() } // Cierra al tocar fuera
    ) {
        // El panel blanco/negro real (ocupa el 82% del ancho)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.82f)
                .background(Color.Black)
                .clickable(enabled = false) {} // Evita que los clics dentro cierren el panel
                .padding(16.dp)
        ) {
            // 1. Cabecera con Perfil y Nombre
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                // Foto de perfil
                if (estadoUsuario.photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = estadoUsuario.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.AccountCircle, null, Modifier.size(50.dp), tint = Color.Gray)
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Nombre del usuario (O fragmento del correo si no hay nombre)
                Column {
                    val nombreAMostrar = if (estadoUsuario.name.isNotEmpty()) {
                        estadoUsuario.name
                    } else if (estadoUsuario.email.isNotEmpty()) {
                        estadoUsuario.email.substringBefore("@")
                    } else {
                        "Usuario"
                    }
                    Text(nombreAMostrar, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Ver perfil", color = Color.Gray, fontSize = 12.sp)
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.White.copy(alpha = 0.2f))

            // 2. Lista de Opciones
            OptionItem(Icons.Default.AddCircle, "Agregar Cuenta")
            
            // Opción para disparar el selector de fotos
            OptionItem(Icons.Default.Image, "Cambiar foto de perfil", alPulsar = {
                lanzadorSelectorFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            })
            
            OptionItem(Icons.Default.History, "Recientes")
            OptionItem(Icons.Default.Campaign, "Tus avisos")
            OptionItem(Icons.Default.Settings, "Configuración y privacidad")
            
            // Opción para cerrar sesión
            OptionItem(Icons.AutoMirrored.Filled.Logout, "Cerrar sesión", alPulsar = {
                vistaModeloAutenticacion.logout()
            })

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Sección de Actividad e Invitación
            Row(modifier = Modifier.fillMaxWidth()) {
                // Columna de Actividad
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (estadoUsuario.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = estadoUsuario.photoUrl,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.AccountCircle, null, Modifier.size(60.dp), tint = Color.Gray)
                    }
                    Text("Actividad", color = Color.White, fontSize = 12.sp)
                    Text("Activar", color = Color.Gray, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Columna de Invitar
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(60.dp).clip(CircleShape).background(Color.DarkGray), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, null, tint = Color.White)
                    }
                    Text("Invitar a amigos", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Sección Mensajes
            Text("Mensajes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Comparte lo que te gusta con tus personas favoritas en Spotify",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
            }
        }
    }
}

/**
 * OptionItem: Fila simple para cada opción del menú.
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
