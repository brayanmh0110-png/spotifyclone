package com.example.spotifyclone

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.spotifyclone.navigation.Screen
import com.example.spotifyclone.navigation.SpotifyNavHost
import com.example.spotifyclone.ui.components.CreateOptionItem
import com.example.spotifyclone.ui.components.MiniPlayer
import com.example.spotifyclone.ui.components.SpotifyBottomBar
import com.example.spotifyclone.ui.theme.SpotifycloneTheme
import com.example.spotifyclone.viewmodel.AuthViewModel
import com.example.spotifyclone.viewmodel.MusicViewModel

/**
 * MainActivity: Es el punto de entrada principal de la aplicación.
 * Se encarga de la configuración global, el manejo de la navegación base
 * y de mostrar elementos persistentes como el MiniPlayer y la Barra de Navegación.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configuración para que la app se vea de borde a borde (detrás de la barra de estado)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            // Aplicamos el tema personalizado de Spotify
            SpotifycloneTheme {
                val context = LocalContext.current
                val navController = rememberNavController()
                
                // Instanciamos los ViewModels que compartirán toda la app
                val authViewModel: AuthViewModel = viewModel()
                val musicViewModel: MusicViewModel = viewModel()

                // Observamos el estado de sesión y la ruta actual
                val estaLogueado by authViewModel.isLoggedIn.collectAsState()
                val rutaActual by navController.currentBackStackEntryAsState()
                val nombreRuta = rutaActual?.destination?.route

                // Estado para el menú flotante de "Crear"
                var mostrarMenuCrear by remember { mutableStateOf(false) }

                // Definimos qué pantallas NO deben mostrar el reproductor o las barras
                val pantallasSinReproductor = listOf(
                    Screen.Welcome.route,
                    Screen.Register.route,
                    Screen.LoginOptions.route,
                    Screen.LoginEmail.route
                )

                // Lógica de redirección automática: si no está logueado, va a Bienvenida.
                LaunchedEffect(estaLogueado) {
                    if (estaLogueado == true) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else if (estaLogueado == false) {
                        musicViewModel.stopMusic()
                        if (nombreRuta !in pantallasSinReproductor) {
                            navController.navigate(Screen.Welcome.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Black,
                        bottomBar = {
                            // Solo mostramos las barras si estamos en pantallas principales
                            val mostrarBarrasGlobales = nombreRuta !in pantallasSinReproductor && 
                                                        nombreRuta != Screen.Player.route && 
                                                        nombreRuta != null
                            
                            if (mostrarBarrasGlobales) {
                                Column(modifier = Modifier.background(Color.Black)) {
                                    // MiniPlayer: El reproductor pequeño que siempre está visible
                                    MiniPlayer(
                                        musicViewModel = musicViewModel,
                                        navController = navController
                                    )
                                    // SpotifyBottomBar: Iconos de Inicio, Buscar y Biblioteca
                                    SpotifyBottomBar(
                                        navController = navController,
                                        onCreateClick = { mostrarMenuCrear = true }
                                    )
                                }
                            }
                        }
                    ) { espaciosInternos ->
                        // Aquí es donde se "pintan" las pantallas según la navegación
                        Box(modifier = Modifier.fillMaxSize().padding(espaciosInternos)) {
                            SpotifyNavHost(
                                navController = navController,
                                authViewModel = authViewModel,
                                musicViewModel = musicViewModel
                            )
                        }
                    }

                    // CAPA DE SUPERPOSICIÓN (Overlay): Menú de "Crear" (Playlist, etc.)
                    if (mostrarMenuCrear) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.7f))
                                .clickable { mostrarMenuCrear = false }
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 110.dp, start = 12.dp, end = 12.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF282828))
                                .padding(20.dp)
                        ) {
                            CreateOptionItem(
                                icon = Icons.Default.MusicNote,
                                title = "Playlist",
                                subtitle = "Crea una playlist con canciones o episodios",
                                alPulsar = {
                                    mostrarMenuCrear = false
                                    navController.navigate(Screen.CreatePlaylist.route)
                                }
                            )
                            CreateOptionItem(
                                icon = Icons.Default.People,
                                title = "Playlist colaborativa",
                                subtitle = "Crea una playlist con tus personas favoritas",
                                alPulsar = {
                                    mostrarMenuCrear = false
                                    Toast.makeText(context, "Playlists colaborativas próximamente", Toast.LENGTH_SHORT).show()
                                }
                            )
                            CreateOptionItem(
                                icon = Icons.Default.AllInclusive,
                                title = "Fusión",
                                subtitle = "Combina los gustos de tus personas favoritas",
                                alPulsar = {
                                    mostrarMenuCrear = false
                                    Toast.makeText(context, "Función Fusión próximamente", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // Botón flotante para cerrar el menú de creación
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 16.dp, end = 16.dp)
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                                .clickable { mostrarMenuCrear = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.Black, modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }
        }
    }
}
