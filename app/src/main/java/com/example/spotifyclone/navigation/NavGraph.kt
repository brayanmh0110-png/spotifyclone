package com.example.spotifyclone.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.spotifyclone.ui.screens.*
import com.example.spotifyclone.viewmodel.AuthViewModel
import com.example.spotifyclone.viewmodel.MusicViewModel

 // Definición de las rutas de la aplicación (Sealed Class)
// Esto asegura que solo existan rutas predefinidas y evita errores de escritura.
sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object Register : Screen("register")
    data object LoginOptions : Screen("login_options")
    data object LoginEmail : Screen("login_email")
    data object Home : Screen("home")
    data object AlbumDetail : Screen("album_detail")
    data object Panelusu : Screen("panel_usu")
    data object LikedSongs : Screen("liked_songs")
}

@Composable
fun SpotifyNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    musicViewModel: MusicViewModel,
    modifier: Modifier = Modifier,
) {
    // NavHost: El contenedor principal que gestiona qué pantalla se muestra según la ruta actual.
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route,
        modifier = modifier
    ) {
        // Cada 'composable' registra una pantalla en el grafo de navegación.
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController = navController)
        }
        
        composable(
            route = Screen.Register.route,
            enterTransition = {
                // Animación de entrada: la pantalla se desliza desde la derecha.
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(500)
                )
            }
        ) {
            RegisterScreen(navController = navController, authViewModel = authViewModel)
        }
        
        composable(
            route = Screen.LoginOptions.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(500)
                )
            }
        ) {
            LoginOptionsScreen(navController = navController)
        }
        
        composable(Screen.LoginEmail.route) {
            LoginEmailScreen(navController = navController, authViewModel = authViewModel)
        }
        
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        composable(Screen.AlbumDetail.route) {
            AlbumDetailScreen(navController = navController, musicViewModel = musicViewModel)
        }
        
        composable(Screen.Panelusu.route) {
            PanelUsuScreen(navController = navController, authViewModel = authViewModel)
        }
        
        composable(Screen.LikedSongs.route) {
            LikedSongsScreen(navController = navController, authViewModel = authViewModel, musicViewModel = musicViewModel)
        }
    }
}
