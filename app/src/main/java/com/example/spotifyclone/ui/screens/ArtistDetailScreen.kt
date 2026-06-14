package com.example.spotifyclone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.spotifyclone.viewmodel.MusicViewModel

/**
 * ArtistDetailScreen: Perfil detallado de un artista.
 * Presenta una imagen de cabecera con degradado y la lista de canciones populares.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    navController: NavHostController,
    musicViewModel: MusicViewModel
) {
    val artista by musicViewModel.artistaActual.collectAsState()
    val canciones by musicViewModel.artistSongs.collectAsState()

    if (artista == null) return

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text(artista!!.name, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            // --- CABECERA VISUAL (Imagen + Nombre) ---
            item {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    AsyncImage(
                        model = artista!!.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Degradado oscuro para que el nombre sea legible
                    Box(
                        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                    )
                    Text(
                        text = artista!!.name,
                        color = Color.White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                    )
                }
            }

            // --- PANEL DE ACCIONES (Shuffle / Play) ---
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { musicViewModel.alternarAleatorio() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Shuffle, null, tint = Color.Gray)
                    }
                    Spacer(Modifier.weight(1f))
                    FloatingActionButton(
                        onClick = { canciones.firstOrNull()?.let { musicViewModel.playSong(it, canciones) } },
                        containerColor = Color(0xFF1DB954),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.Black)
                    }
                }
            }

            // --- LISTA DE CANCIONES ---
            item {
                Text("Populares", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
            }

            items(canciones) { song ->
                LibrarySongItem(
                    song = song,
                    onPlay = { musicViewModel.playSong(song, canciones) },
                    onAddToQueue = { musicViewModel.agregarALaCola(song) },
                    onAddToPlaylist = { /* Abrir diálogo de selección */ }
                )
            }
            
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}
