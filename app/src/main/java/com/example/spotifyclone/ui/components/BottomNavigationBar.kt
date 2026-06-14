package com.example.spotifyclone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.spotifyclone.navigation.Screen

/**
 * SpotifyBottomBar: La barra de navegación principal de la parte inferior.
 * Contiene los accesos a Inicio, Buscar, Biblioteca y el botón de Crear (+).
 */
@Composable
fun SpotifyBottomBar(
    navController: NavHostController,
    onCreateClick: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Row(
        modifier = Modifier.fillMaxWidth().background(Color.Black).padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono Inicio
        BottomNavItem(
            icon = if (currentRoute == Screen.Home.route) Icons.Default.Home else Icons.Outlined.Home,
            label = "Inicio",
            isSelected = currentRoute == Screen.Home.route,
            onClick = {
                if (currentRoute != Screen.Home.route) {
                    navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } }
                }
            }
        )

        // Icono Buscar
        BottomNavItem(
            icon = Icons.Default.Search,
            label = "Buscar",
            isSelected = currentRoute == Screen.Search.route,
            onClick = { if (currentRoute != Screen.Search.route) navController.navigate(Screen.Search.route) }
        )

        // Icono Biblioteca
        BottomNavItem(
            icon = if (currentRoute == Screen.Library.route) Icons.Default.LibraryMusic else Icons.Outlined.LibraryMusic,
            label = "Tu biblioteca",
            isSelected = currentRoute == Screen.Library.route,
            onClick = { if (currentRoute != Screen.Library.route) navController.navigate(Screen.Library.route) }
        )

        // Botón especial: Crear (+)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { onCreateClick() }
        ) {
            Icon(Icons.Default.Add, null, tint = Color.Gray, modifier = Modifier.size(28.dp))
            Text("Crear", color = Color.Gray, fontSize = 10.sp)
        }
    }
}

/**
 * BottomNavItem: Representa un botón individual de la barra inferior.
 */
@Composable
fun BottomNavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(icon, null, tint = if (isSelected) Color.White else Color.Gray, modifier = Modifier.size(28.dp))
        Text(label, color = if (isSelected) Color.White else Color.Gray, fontSize = 10.sp)
    }
}

/**
 * CreateOptionItem: Fila reutilizable para el menú que aparece al pulsar "Crear (+)".
 */
@Composable
fun CreateOptionItem(icon: ImageVector, title: String, subtitle: String, alPulsar: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { alPulsar() }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, color = Color.White, fontSize = 16.sp)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
    }
}
