package com.example.spotifyclone.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
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
import com.example.spotifyclone.model.Song

/**
 * LibraryScreen: Pantalla de la biblioteca personal del usuario.
 * Muestra una lista vertical de todas las canciones disponibles en el sistema.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    controladorNavegacion: NavHostController,
    vistaModeloMusica: MusicViewModel
) {
    // Obtenemos la lista de canciones global desde el ViewModel
    val listaCanciones by vistaModeloMusica.songs.collectAsState()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            // Barra superior centrada con estilo Spotify
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Tu Biblioteca",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { controladorNavegacion.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { rellenos ->
        Column(modifier = Modifier.padding(rellenos)) {
            // Contador de canciones
            Text(
                text = "Todas las canciones (${listaCanciones.size})",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Lista scrollable de canciones
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(listaCanciones) { cancion ->
                    ItemCancionBiblioteca(cancion) {
                        // Al pulsar, reproducimos la canción y pasamos la lista actual
                        vistaModeloMusica.playSong(cancion, listaCanciones)
                    }
                }
                // Espacio extra al final para que el mini-reproductor no tape la última canción
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

/**
 * ItemCancionBiblioteca: Representa una fila individual de una canción.
 */
@Composable
fun ItemCancionBiblioteca(cancion: Song, alPulsar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { alPulsar() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Carátula de la canción
        AsyncImage(
            model = cancion.coverUrl,
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Información de texto
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cancion.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = "Canción • ${cancion.artist}",
                color = Color.Gray,
                fontSize = 14.sp,
                maxLines = 1
            )
        }
        
        // Icono de opciones (tres puntos)
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Opciones",
            tint = Color.Gray
        )
    }
}
