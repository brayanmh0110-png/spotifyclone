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
 * MainActivity: Es el corazón de la aplicación.
 * Aquí se configura el diseño base, la navegación y los elementos globales.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configura la app para que ocupe toda la pantalla y fuerza iconos blancos en la barra de estado (Hora/Batería)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            SpotifycloneTheme {
                // Herramientas esenciales para que la app funcione:
                val context = LocalContext.current
                val controladorNavegacion = rememberNavController()
                val vistaModeloAutenticacion: AuthViewModel = viewModel()
                val vistaModeloMusica: MusicViewModel = viewModel()

                // Estados que observamos en tiempo real:
                val estaLogueado by vistaModeloAutenticacion.isLoggedIn.collectAsState()
                val rutaActual by controladorNavegacion.currentBackStackEntryAsState()
                val nombreRuta = rutaActual?.destination?.route

                // Estado para mostrar u ocultar el menú de "Crear" (+)
                var mostrarMenuCrear by remember { mutableStateOf(false) }

                // Lista de pantallas donde NO queremos mostrar controles de música (flujo de login)
                val pantallasSinReproductor = listOf(
                    Screen.Welcome.route,
                    Screen.Register.route,
                    Screen.LoginOptions.route,
                    Screen.LoginEmail.route
                )

                // Lógica de redirección automática: si cambia el estado del login, movemos al usuario
                LaunchedEffect(estaLogueado) {
                    if (estaLogueado == true) {
                        controladorNavegacion.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true } // Limpia el historial para no volver atrás
                        }
                    } else if (estaLogueado == false) {
                        // AQUÍ: Detenemos la música si el usuario cierra sesión
                        vistaModeloMusica.stopMusic()
                        
                        if (nombreRuta !in pantallasSinReproductor) {
                            controladorNavegacion.navigate(Screen.Welcome.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }

                // Estructura visual principal
                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Black, // FONTO NEGRO PARA EVITAR BORDES BLANCOS
                        // Barra inferior persistente (Bottom Bar)
                        bottomBar = {
                            // Solo se muestra si el usuario ya entró a la app principal
                            val mostrarBarrasGlobales = nombreRuta !in pantallasSinReproductor && 
                                                        nombreRuta != Screen.Player.route && 
                                                        nombreRuta != null
                            
                            if (mostrarBarrasGlobales) {
                                Column(modifier = Modifier.background(Color.Black)) {
                                    // Mini-reproductor que flota sobre el menú
                                    MiniPlayer(
                                        musicViewModel = vistaModeloMusica,
                                        navController = controladorNavegacion
                                    )
                                    // El menú de 5 botones (Inicio, Buscar, etc.)
                                    SpotifyBottomBar(
                                        navController = controladorNavegacion,
                                        onCreateClick = { mostrarMenuCrear = true }
                                    )
                                }
                            }
                        }
                    ) { espaciosInternos ->
                        // Contenedor de las pantallas (Home, Buscar, Biblioteca, etc.)
                        Box(modifier = Modifier.fillMaxSize().padding(espaciosInternos)) {
                            SpotifyNavHost(
                                navController = controladorNavegacion,
                                authViewModel = vistaModeloAutenticacion,
                                musicViewModel = vistaModeloMusica
                            )
                        }
                    }

                    // CAPA DE SUPERPOSICIÓN: Menú de "Crear" (Solo aparece al pulsar el +)
                    if (mostrarMenuCrear) {
                        // 1. Fondo oscuro que bloquea el resto de la app
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.7f))
                                .clickable { mostrarMenuCrear = false } // Cerrar al tocar fuera
                        )

                        // 2. Tarjeta con opciones de creación (Playlist, Fusión, etc.)
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 110.dp, start = 12.dp, end = 12.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF282828))
                                .padding(20.dp)
                                .clickable(enabled = false) { }
                        ) {
                            CreateOptionItem(
                                icon = Icons.Default.MusicNote,
                                title = "Playlist",
                                subtitle = "Crea una playlist con canciones o episodios",
                                alPulsar = {
                                    mostrarMenuCrear = false
                                    controladorNavegacion.navigate(Screen.CreatePlaylist.route)
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
                                subtitle = "Combina los gustos de tus personas favoritas en una playlist",
                                alPulsar = {
                                    mostrarMenuCrear = false
                                    Toast.makeText(context, "Función Fusión próximamente", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // 3. Botón circular "X" para cerrar el menú
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
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Black, modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }
        }
    }
}
