package com.example.spotifyclone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.spotifyclone.viewmodel.MusicViewModel

/**
 * QueueScreen: Pantalla que muestra la lista de canciones próximas a sonar.
 * Permite reordenar la cola y eliminar canciones de la lista actual.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    navController: NavHostController,
    musicViewModel: MusicViewModel
) {
    val currentSong by musicViewModel.currentSong.collectAsState()
    val playlist by musicViewModel.currentPlaylist.collectAsState()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Cola de reproducción", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            // --- CANCIÓN QUE SUENA AHORA ---
            item {
                Text("Reproduciendo ahora", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))
                currentSong?.let { song ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(model = song.coverUrl, null, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(song.title, color = Color(0xFF1DB954), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text(song.artist, color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text("Siguiente de la cola", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
            }

            // --- RESTO DE LA COLA ---
            itemsIndexed(playlist) { index, song ->
                if (song.id != currentSong?.id) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(model = song.coverUrl, null, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(4.dp)).background(Color.DarkGray), contentScale = ContentScale.Crop)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(song.title, color = Color.White, fontSize = 15.sp)
                            Text(song.artist, color = Color.Gray, fontSize = 13.sp)
                        }
                        
                        // Controles para reordenar la cola manualmente
                        Column {
                            IconButton(onClick = { musicViewModel.reordenarCola(index, (index - 1).coerceAtLeast(0)) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.KeyboardArrowUp, null, tint = Color.Gray)
                            }
                            IconButton(onClick = { musicViewModel.reordenarCola(index, (index + 1).coerceAtMost(playlist.size - 1)) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.Gray)
                            }
                        }
                        
                        // Botón para quitar de la cola
                        IconButton(onClick = { musicViewModel.quitarDeCola(song.id) }) {
                            Icon(Icons.Default.RemoveCircleOutline, null, tint = Color.Gray)
                        }
                    }
                }
            }
            
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}
