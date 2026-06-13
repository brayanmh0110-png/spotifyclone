package com.example.spotifyclone.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.spotifyclone.navigation.Screen
import com.example.spotifyclone.viewmodel.AuthViewModel
import com.example.spotifyclone.viewmodel.MusicViewModel

/**
 * CrearPlaylistScreen: Pantalla para ponerle nombre a una nueva playlist.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPlaylistScreen(
    navController: NavHostController,
    musicViewModel: MusicViewModel,
    authViewModel: AuthViewModel
) {
    val userState by authViewModel.userState.collectAsState()
    var playlistName by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Crear playlist", color = Color.White, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, "Cerrar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            Text(
                text = "Ponle un nombre a tu playlist",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = playlistName,
                onValueChange = { playlistName = it },
                placeholder = { Text("Mi playlist", color = Color.Gray) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF1DB954),
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color(0xFF1DB954)
                )
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    if (playlistName.isNotBlank()) {
                        musicViewModel.crearPlaylist(
                            userState.uid,
                            playlistName.trim()
                        ) { newId ->
                            navController.navigate(
                                Screen.PlaylistDetail.crearRuta(newId)
                            ) {
                                popUpTo(Screen.CreatePlaylist.route) { inclusive = true }
                            }
                        }
                    }
                },
                enabled = playlistName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1DB954),
                    disabledContainerColor = Color(0xFF1DB954).copy(alpha = 0.4f)
                )
            ) {
                Text("Crear", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
