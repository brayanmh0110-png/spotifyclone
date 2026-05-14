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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpotifycloneTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val musicViewModel: MusicViewModel = viewModel()

                val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Pantallas donde NO se debe mostrar el mini reproductor
                val noPlayerScreens = listOf(
                    Screen.Welcome.route,
                    Screen.Register.route,
                    Screen.LoginOptions.route,
                    Screen.LoginEmail.route
                )

                // Manejo de la navegación según el estado de la sesión
                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn == true) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else if (isLoggedIn == false) {
                        // Si cerramos sesión y no estamos en una pantalla de auth, volvemos a Welcome
                        if (currentRoute !in noPlayerScreens) {
                            navController.navigate(Screen.Welcome.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        SpotifyNavHost(
                            navController = navController,
                            authViewModel = authViewModel,
                            musicViewModel = musicViewModel
                        )
                        
                        // Si la ruta actual no está en la lista negra, mostramos el mini-player
                        if (currentRoute !in noPlayerScreens) {
                            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                                MiniPlayer(musicViewModel = musicViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
