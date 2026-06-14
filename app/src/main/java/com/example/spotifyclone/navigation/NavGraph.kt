package com.example.spotifyclone.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
 * Screen: Clase sellada que define todas las rutas (direcciones) de la app.
 * Esto evita errores de escritura al navegar entre pantallas.
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
    data object Queue : Screen("queue")
    data object CreatePlaylist : Screen("create_playlist")
    data object Search : Screen("search")
    data object ActivityLog : Screen("activity_log")
    
    // Rutas con parámetros dinámicos (ID del artista o ID de la playlist)
    data object ArtistDetail : Screen("artist_detail/{artistId}") {
        fun crearRuta(artistId: String) = "artist_detail/$artistId"
    }
    data object PlaylistDetail : Screen("playlist_detail/{playlistId}") {
        fun crearRuta(playlistId: String) = "playlist_detail/$playlistId"
    }
}

/**
 * SpotifyNavHost: Es el "controlador de tráfico" de la aplicación.
 * Aquí se registra cada pantalla y se define su animación de entrada/salida.
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
        startDestination = Screen.Welcome.route, // La app empieza en la pantalla de Bienvenida
        modifier = modifier,
        // Animaciones globales por defecto
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        // --- SECCIÓN: AUTENTICACIÓN ---
        
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController = navController)
        }
        
        composable(
            route = Screen.Register.route,
            enterTransition = {
                slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500))
            },
            exitTransition = {
                slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500))
            }
        ) {
            RegisterScreen(navController = navController, authViewModel = authViewModel)
        }
        
        composable(Screen.LoginOptions.route) {
            LoginOptionsScreen(navController = navController)
        }
        
        composable(Screen.LoginEmail.route) {
            LoginEmailScreen(navController = navController, authViewModel = authViewModel)
        }
        
        // --- SECCIÓN: PANTALLAS PRINCIPALES ---
        
        composable(Screen.Home.route) {
            HomeScreen(navController = navController, musicViewModel = musicViewModel, authViewModel = authViewModel)
        }
        
        composable(
            Screen.AlbumDetail.route,
            enterTransition = {
                slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up, animationSpec = tween(500))
            }
        ) {
            AlbumDetailScreen(navController = navController, musicViewModel = musicViewModel, authViewModel = authViewModel)
        }
        
        // El panel de usuario se muestra como un Diálogo (pop-up)
        dialog(
            route = Screen.Panelusu.route,
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            PanelUsuScreen(navController = navController, authViewModel = authViewModel)
        }
        
        composable(Screen.LikedSongs.route) {
            LikedSongsScreen(navController = navController, authViewModel = authViewModel, musicViewModel = musicViewModel)
        }

        composable(Screen.Library.route) {
            LibraryScreen(navController = navController, musicViewModel = musicViewModel, authViewModel = authViewModel)
        }

        composable(Screen.CreatePlaylist.route) {
            CrearPlaylistScreen(navController = navController, musicViewModel = musicViewModel, authViewModel = authViewModel)
        }

        composable(Screen.Queue.route) {
            QueueScreen(navController = navController, musicViewModel = musicViewModel)
        }

        composable(Screen.PlaylistDetail.route) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
            PlaylistScreen(playlistId = playlistId, navController = navController, musicViewModel = musicViewModel, authViewModel = authViewModel)
        }

        composable(Screen.Search.route) {
            SearchScreen(navController = navController, musicViewModel = musicViewModel, authViewModel = authViewModel)
        }

        composable(Screen.ActivityLog.route) {
            ActivityLogScreen(navController = navController, musicViewModel = musicViewModel, authViewModel = authViewModel)
        }

        composable(Screen.ArtistDetail.route) {
            ArtistDetailScreen(navController = navController, musicViewModel = musicViewModel)
        }

        // --- SECCIÓN: REPRODUCTOR ---
        
        composable(
            route = Screen.Player.route,
            enterTransition = {
                slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up, animationSpec = tween(500))
            },
            exitTransition = {
                slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down, animationSpec = tween(500))
            }
        ) {
            PlayerScreen(navController = navController, musicViewModel = musicViewModel, authViewModel = authViewModel)
        }
    }
}
