package com.example.spotifyclone.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.material3.Text
import com.example.spotifyclone.ui.screens.*
import com.example.spotifyclone.viewmodel.AuthViewModel

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
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route,
        modifier = modifier
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController = navController)
        }
        
        composable(
            route = Screen.Register.route,
            enterTransition = {
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
            AlbumDetailScreen(navController = navController)
        }
        
        composable(Screen.Panelusu.route) {
            PanelUsuScreen(navController = navController)
        }
        
        composable(Screen.LikedSongs.route) {
            LikedSongsScreen(navController = navController)
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Text(text = "This is the $name")
}
