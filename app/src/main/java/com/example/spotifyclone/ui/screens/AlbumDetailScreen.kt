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
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.spotifyclone.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(navController: NavHostController) {
    val songs = listOf(
        Song(title = "Sea Bendito", artist = "Miel San Marcos"),
        Song(title = "Increíble", artist = "Miel San Marcos"),
        Song(title = "Agradecido", artist = "Miel San Marcos"),
        Song(title = "No Hay Lugar Más Alto", artist = "Miel San Marcos"),
        Song(title = "Danzo En El Río", artist = "Miel San Marcos"),
        Song(title = "Grande y Fuerte", artist = "Miel San Marcos"),
        Song(title = "Regocíjate Oh Israel", artist = "Miel San Marcos"),
        Song(title = "Levántate Señor", artist = "Miel San Marcos"),
        Song(title = "Vino Celestial", artist = "Miel San Marcos"),
        Song(title = "Proclamaré Victoria", artist = "Miel San Marcos"),
        Song(title = "Jehová de los Ejércitos", artist = "Miel San Marcos"),
        Song(title = "El Gozo del Señor", artist = "Miel San Marcos"),
        Song(title = "Fiesta Hay en el Corazón", artist = "Miel San Marcos"),
        Song(title = "Digno es el Señor", artist = "Miel San Marcos"),
        Song(title = "Tu Presencia es el Cielo", artist = "Miel San Marcos"),
        Song(title = "Amamos tu Presencia", artist = "Miel San Marcos"),
        Song(title = "Exáltate", artist = "Miel San Marcos"),
        Song(title = "Glorifícate", artist = "Miel San Marcos"),
        Song(title = "Soberano", artist = "Miel San Marcos"),
        Song(title = "Rey Vencedor", artist = "Miel San Marcos"),
        Song(title = "Abba Padre", artist = "Miel San Marcos"),
        Song(title = "Eres Dios", artist = "Miel San Marcos"),
        Song(title = "Canto de Victoria", artist = "Miel San Marcos"),
        Song(title = "Poderoso Dios", artist = "Miel San Marcos"),
        Song(title = "Josué 1:9", artist = "Miel San Marcos")
    )

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                AlbumHeader()
            }

            item {
                AlbumInfo()
            }

            item {
                ActionButtons()
            }

            items(songs) { song ->
                SongRow(song)
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun AlbumHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Gray, Color.Black)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(200.dp),
            shadowElevation = 8.dp,
            color = Color.DarkGray
        ) {
            Image(
                painter = painterResource(id = R.drawable.album_generaciones),
                contentDescription = "Portada del Álbum",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun AlbumInfo() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Dios de Generaciones (En Vivo)",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.mielsan),
                contentDescription = "Artista",
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Miel San Marcos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Álbum · 1 ene. 2025", color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
fun ActionButtons() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.AddCircleOutline, null, tint = Color.Gray, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(24.dp))
        Icon(Icons.Default.DownloadForOffline, null, tint = Color.Gray, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(24.dp))
        Icon(Icons.Default.MoreVert, null, tint = Color.Gray, modifier = Modifier.size(28.dp))

        Spacer(modifier = Modifier.weight(1f))

        Icon(Icons.Default.Shuffle, null, tint = Color(0xFF1DB954), modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF1DB954)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun SongRow(song: Song) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = song.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(text = song.artist, color = Color.Gray, fontSize = 12.sp)
        }
        Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
    }
}
