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
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.spotifyclone.R
import com.example.spotifyclone.viewmodel.MusicViewModel
import coil.compose.AsyncImage
import com.example.spotifyclone.model.Album
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AlbumDetailScreen(navController: NavHostController, musicViewModel: MusicViewModel) {
    val songs by musicViewModel.albumSongs.collectAsState()
    val albums by musicViewModel.albums.collectAsState()
    
    // For now, let's assume we show the first album if available
    val album = albums.firstOrNull() ?: Album(
        title = "Dios de Generaciones (En Vivo)",
        artist = "Miel San Marcos",
        coverUrl = "" // Fallback
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
                AlbumHeader(album.coverUrl)
            }

            item {
                AlbumInfo(album)
            }

            item {
                ActionButtons()
            }

            items(songs) { song ->
                SongRow(song) {
                    musicViewModel.playSong(song)
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun AlbumHeader(coverUrl: String) {
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
            if (coverUrl.isNotEmpty()) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = "Portada del Álbum",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.album_generaciones),
                    contentDescription = "Portada del Álbum",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun AlbumInfo(album: Album) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = album.title,
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
            Text(text = album.artist, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Álbum · ${album.releaseDate}", color = Color.Gray, fontSize = 14.sp)
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
fun SongRow(song: Song, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
