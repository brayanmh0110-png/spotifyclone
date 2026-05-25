package com.example.spotifyclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.spotifyclone.navigation.Screen
import com.example.spotifyclone.navigation.SpotifyNavHost
import com.example.spotifyclone.ui.components.MiniPlayer
import com.example.spotifyclone.ui.theme.SpotifycloneTheme
import com.example.spotifyclone.viewmodel.AuthViewModel
import com.example.spotifyclone.viewmodel.MusicViewModel

/**
 * Actividad principal de la aplicación.
 * Actúa como el punto de entrada y gestiona la estructura base de la UI.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Habilita el diseño de borde a borde (detrás de las barras del sistema)
        setContent {
            SpotifycloneTheme {
                // Controladores de navegación y ViewModels compartidos
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val musicViewModel: MusicViewModel = viewModel()

                // Observadores de estado global
                val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Configuración de pantallas que no muestran controles de música (Auth Flow)
                val noPlayerScreens = listOf(
                    Screen.Welcome.route,
                    Screen.Register.route,
                    Screen.LoginOptions.route,
                    Screen.LoginEmail.route
                )

                // Efecto para redirigir según el estado de la sesión de Firebase
                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn == true) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else if (isLoggedIn == false) {
                        if (currentRoute !in noPlayerScreens) {
                            navController.navigate(Screen.Welcome.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        // El Grafo de navegación principal
                        SpotifyNavHost(
                            navController = navController,
                            authViewModel = authViewModel,
                            musicViewModel = musicViewModel
                        )
                        
                        // Mini-reproductor global (visible si hay una canción y no es pantalla de auth)
                        if (currentRoute !in noPlayerScreens && currentRoute != Screen.Player.route) {
                            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                                MiniPlayer(
                                    musicViewModel = musicViewModel,
                                    navController = navController
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
