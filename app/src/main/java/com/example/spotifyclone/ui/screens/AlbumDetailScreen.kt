package com.example.spotifyclone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.spotifyclone.model.Song
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import com.example.spotifyclone.viewmodel.MusicViewModel
import coil.compose.AsyncImage
import com.example.spotifyclone.model.Album
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

/**
 * AlbumDetailScreen: Pantalla que muestra el contenido de un álbum específico.
 * Incluye cabecera con degradado, información del artista y lista de pistas.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AlbumDetailScreen(
    controladorNavegacion: NavHostController, 
    vistaModeloMusica: MusicViewModel
) {
    // Obtenemos las canciones filtradas por álbum y la lista global de álbumes
    val listaCancionesAlbum by vistaModeloMusica.albumSongs.collectAsState()
    val listaTodosAlbumes by vistaModeloMusica.albums.collectAsState()
    
    // Obtenemos el álbum actual (por defecto el primero para esta demo)
    val albumActual = listaTodosAlbumes.firstOrNull() ?: Album(
        title = "Cargando...",
        artist = "...",
        coverUrl = ""
    )

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { controladorNavegacion.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { rellenos ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(rellenos)
        ) {
            // 1. Cabecera visual con la portada
            item {
                CabeceraImagenAlbum(albumActual.coverUrl)
            }

            // 2. Título y nombre del artista
            item {
                InformacionTextoAlbum(albumActual)
            }

            // 3. Fila de botones (Play, Shuffle, Descarga)
            item {
                PanelAccionesAlbum()
            }

            // 4. Lista de pistas del álbum
            items(listaCancionesAlbum) { cancion ->
                FilaCancionAlbum(cancion) {
                    vistaModeloMusica.playSong(cancion, listaCancionesAlbum)
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun CabeceraImagenAlbum(urlPortada: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Gray.copy(alpha = 0.5f), Color.Black)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(200.dp),
            shadowElevation = 12.dp,
            color = Color.DarkGray
        ) {
            if (urlPortada.isNotEmpty()) {
                AsyncImage(
                    model = urlPortada,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(Modifier.fillMaxSize().background(Color.DarkGray)) {
                    Icon(Icons.Default.Audiotrack, null, Modifier.align(Alignment.Center).size(80.dp), tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun InformacionTextoAlbum(album: Album) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = album.title,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icono pequeño del artista
            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.Gray))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = album.artist, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Álbum • ${album.releaseDate}", color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
fun PanelAccionesAlbum() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.AddCircleOutline, null, tint = Color.Gray, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(24.dp))
        Icon(Icons.Default.DownloadForOffline, null, tint = Color.Gray, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(24.dp))
        Icon(Icons.Default.MoreVert, null, tint = Color.Gray, modifier = Modifier.size(28.dp))

        Spacer(Modifier.weight(1f))

        Icon(Icons.Default.Shuffle, null, tint = Color(0xFF1DB954), modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(16.dp))
        // Botón Play Circular
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFF1DB954)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun FilaCancionAlbum(cancion: Song, alPulsar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { alPulsar() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = cancion.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(text = cancion.artist, color = Color.Gray, fontSize = 12.sp)
        }
        Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
    }
}
