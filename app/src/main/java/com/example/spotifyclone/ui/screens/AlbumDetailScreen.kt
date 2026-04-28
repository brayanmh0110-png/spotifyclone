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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AlbumDetailScreen(navController: NavHostController) {
    val songs = listOf(
        Song("Sea Bendito", "Miel San Marcos"),
        Song("Increíble", "Miel San Marcos"),
        Song("Agradecido", "Miel San Marcos"),
        Song("No Hay Lugar Más Alto", "Miel San Marcos"),
        Song("Danzo En El Río", "Miel San Marcos"),
        Song("Grande y Fuerte", "Miel San Marcos"),
        Song("Regocíjate Oh Israel", "Miel San Marcos"),
        Song("Levántate Señor", "Miel San Marcos"),
        Song("Vino Celestial", "Miel San Marcos"),
        Song("Proclamaré Victoria", "Miel San Marcos"),
        Song("Jehová de los Ejércitos", "Miel San Marcos"),
        Song("El Gozo del Señor", "Miel San Marcos"),
        Song("Fiesta Hay en el Corazón", "Miel San Marcos"),
        Song("Digno es el Señor", "Miel San Marcos"),
        Song("Tu Presencia es el Cielo", "Miel San Marcos"),
        Song("Amamos tu Presencia", "Miel San Marcos"),
        Song("Exáltate", "Miel San Marcos"),
        Song("Glorifícate", "Miel San Marcos"),
        Song("Soberano", "Miel San Marcos"),
        Song("Rey Vencedor", "Miel San Marcos"),
        Song("Abba Padre", "Miel San Marcos"),
        Song("Eres Dios", "Miel San Marcos"),
        Song("Canto de Victoria", "Miel San Marcos"),
        Song("Poderoso Dios", "Miel San Marcos"),
        Song("Josué 1:9", "Miel San Marcos")
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
            Text(text = song.artists, color = Color.Gray, fontSize = 12.sp)
        }
        Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
    }
}
