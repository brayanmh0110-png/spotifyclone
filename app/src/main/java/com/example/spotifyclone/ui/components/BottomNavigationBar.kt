package com.example.spotifyclone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.spotifyclone.navigation.Screen

/**
 * SpotifyBottomBar: El menú de navegación de la parte inferior.
 * Proporciona acceso rápido a las secciones principales.
 * 
 * @param navController Controlador para manejar el cambio de pantallas.
 * @param onCreateClick Función que se ejecuta al pulsar el botón "Crear" (+).
 */
@Composable
fun SpotifyBottomBar(navController: NavHostController, onCreateClick: () -> Unit) {
    
    // Contenedor de la barra de navegación (Material Design 3)
    NavigationBar(containerColor = Color.Black.copy(alpha = 0.9f)) {
        
        // Lista de botones de la barra (Texto, Icono, Ruta)
        val botonesNavegacion = listOf(
            Triple("Inicio", Icons.Default.Home, Screen.Home.route),
            Triple("Buscar", Icons.Default.Search, Screen.Search.route),
            Triple("Biblioteca", Icons.Default.LibraryMusic, Screen.Library.route),
            Triple("Premium", Icons.Default.WorkspacePremium, ""), // Ruta vacía para demostración
            Triple("Crear", Icons.Default.Add, "crear") // Caso especial: abre un menú en vez de navegar
        )
        
        // Obtenemos la ruta actual para saber qué botón resaltar
        val estadoRutaActual by navController.currentBackStackEntryAsState()
        val rutaActiva = estadoRutaActual?.destination?.route

        // Dibujamos cada botón de la lista
        botonesNavegacion.forEach { (titulo, icono, ruta) ->
            NavigationBarItem(
                // Se marca como seleccionado si la ruta coincide
                selected = rutaActiva == ruta,
                onClick = {
                    when (ruta) {
                        "crear" -> onCreateClick() // Avisa a MainActivity que abra el menú
                        "" -> { /* Opción no implementada aún */ }
                        else -> {
                            // Navegación inteligente (evita duplicar pantallas en el historial)
                            if (rutaActiva != ruta) {
                                navController.navigate(ruta) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true // Solo una copia de la pantalla a la vez
                                    restoreState = true // Recupera el scroll y estado si ya existía
                                }
                            }
                        }
                    }
                },
                icon = { Icon(icono, contentDescription = titulo) },
                label = { Text(titulo, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = Color.White,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent // Quita el círculo de fondo al seleccionar
                )
            )
        }
    }
}

/**
 * CreateOptionItem: Componente reutilizable para las opciones dentro del menú "Crear".
 * 
 * @param icon El icono representativo de la opción.
 * @param title Título principal (ej: Playlist).
 * @param subtitle Descripción breve (ej: Crea una playlist...).
 */
@Composable
fun CreateOptionItem(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Aquí se implementaría la creación real */ }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono dentro de un círculo estilizado
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF3E3E3E)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Textos descriptivos
        Column {
            Text(
                text = title, 
                color = Color.White, 
                fontSize = 16.sp, 
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Text(
                text = subtitle, 
                color = Color.Gray, 
                fontSize = 12.sp
            )
        }
    }
}
