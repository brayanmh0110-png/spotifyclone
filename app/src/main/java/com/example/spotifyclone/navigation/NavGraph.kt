package com.example.spotifyclone.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.dialog
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.spotifyclone.ui.screens.*
import com.example.spotifyclone.viewmodel.AuthViewModel
import com.example.spotifyclone.viewmodel.MusicViewModel

/**
 * Screen: Definición de las rutas de navegación de la aplicación.
 * El uso de una Sealed Class garantiza seguridad de tipos y evita errores de texto.
 */
sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object Register : Screen("register")
    data object LoginOptions : Screen("login_options")
    data object LoginEmail : Screen("login_email")
    data object Home : Screen("home")
    data object AlbumDetail : Screen("album_detail")
    data object Panelusu : Screen("panel_usu")
    data object LikedSongs : Screen("liked_songs")
    data object Player : Screen("player")
    data object Library : Screen("library")
    data object CreatePlaylist : Screen("create_playlist")
    data object Search : Screen("search")
    data object ActivityLog : Screen("activity_log")
    data object PlaylistDetail : Screen("playlist_detail/{playlistId}") {
        fun crearRuta(playlistId: String) = "playlist_detail/$playlistId"
    }
}

/**
 * SpotifyNavHost: El mapa de navegación de la aplicación.
 * Define qué pantalla mostrar según la ruta activa y cómo transicionar entre ellas.
 */
@Composable
fun SpotifyNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    musicViewModel: MusicViewModel,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route,
        modifier = modifier
    ) {
        // --- FLUJO DE BIENVENIDA Y AUTENTICACIÓN ---
        
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController = navController)
        }
        
        composable(
            route = Screen.Register.route,
            enterTransition = {
                slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500))
            }
        ) {
            RegisterScreen(
                navController = navController, 
                authViewModel = authViewModel
            )
        }
        
        composable(
            route = Screen.LoginOptions.route,
            enterTransition = {
                slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500))
            }
        ) {
            LoginOptionsScreen(navController = navController)
        }
        
        composable(Screen.LoginEmail.route) {
            LoginEmailScreen(
                navController = navController, 
                authViewModel = authViewModel
            )
        }
        
        // --- PANTALLAS PRINCIPALES (POST-LOGIN) ---
        
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController, 
                musicViewModel = musicViewModel, 
                authViewModel = authViewModel
            )
        }
        
        composable(Screen.AlbumDetail.route) {
            AlbumDetailScreen(
                navController = navController,
                musicViewModel = musicViewModel,
                authViewModel = authViewModel
            )
        }
        
        // Panel de usuario como diálogo (overlay)
        dialog(
            route = Screen.Panelusu.route,
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            PanelUsuScreen(
                navController = navController, 
                authViewModel = authViewModel
            )
        }
        
        composable(Screen.LikedSongs.route) {
            LikedSongsScreen(
                navController = navController, 
                authViewModel = authViewModel, 
                musicViewModel = musicViewModel
            )
        }

        composable(Screen.Library.route) {
            LibraryScreen(
                navController = navController,
                musicViewModel = musicViewModel,
                authViewModel = authViewModel
            )
        }

        composable(Screen.CreatePlaylist.route) {
            CrearPlaylistScreen(
                navController = navController,
                musicViewModel = musicViewModel,
                authViewModel = authViewModel
            )
        }

        composable(Screen.PlaylistDetail.route) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
            PlaylistScreen(
                playlistId = playlistId,
                navController = navController,
                musicViewModel = musicViewModel,
                authViewModel = authViewModel
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                navController = navController, 
                musicViewModel = musicViewModel
            )
        }

        composable(Screen.ActivityLog.route) {
            ActivityLogScreen(
                navController = navController,
                musicViewModel = musicViewModel,
                authViewModel = authViewModel
            )
        }

        // --- REPRODUCTOR A PANTALLA COMPLETA ---
        
        composable(
            route = Screen.Player.route,
            enterTransition = {
                slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up, animationSpec = tween(500))
            },
            exitTransition = {
                slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down, animationSpec = tween(500))
            }
        ) {
            PlayerScreen(
                navController = navController, 
                musicViewModel = musicViewModel, 
                authViewModel = authViewModel
            )
        }
    }
}
