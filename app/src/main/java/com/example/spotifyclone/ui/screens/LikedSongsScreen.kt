package com.example.spotifyclone.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.spotifyclone.R
import com.example.spotifyclone.viewmodel.AuthViewModel
import com.example.spotifyclone.viewmodel.MusicViewModel

@Composable
fun LikedSongsScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    musicViewModel: MusicViewModel
) {
    val likedSongs = listOf(
        VisualSong("Sea Bendito", "Miel San Marcos", R.drawable.album_generaciones),
        VisualSong("Increíble", "Miel San Marcos", R.drawable.album_generaciones),
        VisualSong("Agradecido", "Miel San Marcos", R.drawable.album_generaciones),
        VisualSong("No Hay Lugar Más Alto", "Miel San Marcos", R.drawable.album_generaciones),
        VisualSong("Danzo En El Río", "Miel San Marcos", R.drawable.album_generaciones),
        VisualSong("Grande y Fuerte", "Miel San Marcos", R.drawable.album_generaciones),
        VisualSong("Regocíjate Oh Israel", "Miel San Marcos", R.drawable.album_generaciones),
        VisualSong("Levántate Señor", "Miel San Marcos", R.drawable.album_generaciones),
        VisualSong("Vino Celestial", "Miel San Marcos", R.drawable.album_generaciones),
        VisualSong("Proclamaré Victoria", "Miel San Marcos", R.drawable.album_generaciones),
        VisualSong("Jehová de los Ejércitos", "Miel San Marcos", R.drawable.album_generaciones),
        VisualSong("El Gozo del Señor", "Miel San Marcos", R.drawable.album_generaciones),
        VisualSong("Fiesta Hay en el Corazón", "Miel San Marcos", R.drawable.album_generaciones),
        VisualSong("Digno es el Señor", "Miel San Marcos", R.drawable.album_generaciones),
        VisualSong("Tu Presencia es el Cielo", "Miel San Marcos", R.drawable.album_generaciones),
        VisualSong("Amamos tu Presencia", "Miel San Marcos", R.drawable.album_generaciones),
        VisualSong("Exáltate", "Miel San Marcos", R.drawable.album_generaciones),
        VisualSong("Glorifícate", "Miel San Marcos", R.drawable.album_generaciones),
        VisualSong("Soberano", "Miel San Marcos", R.drawable.album_generaciones),
        VisualSong("Rey Vencedor", "Miel San Marcos", R.drawable.album_generaciones)
    )

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                Row {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.White)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más", tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                LikedHeader(likedSongs.size)
            }
            items(likedSongs) { song ->
                LikedSongItem(song)
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun LikedHeader(count: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF503691), Color.Black)
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Tus me gustas",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$count canciones",
            color = Color.LightGray,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1DB954)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Reproducir",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun LikedSongItem(song: VisualSong) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = song.imageRes),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = song.artist,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Opciones",
            tint = Color.Gray
        )
    }
}

data class VisualSong(val title: String, val artist: String, val imageRes: Int)
