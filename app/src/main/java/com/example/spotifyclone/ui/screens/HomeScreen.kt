package com.example.spotifyclone.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.spotifyclone.navigation.Screen
import com.example.spotifyclone.R
import coil.compose.AsyncImage
import com.example.spotifyclone.viewmodel.MusicViewModel
import com.example.spotifyclone.viewmodel.AuthViewModel

/**
 * HomeScreen: Pantalla principal de la aplicación.
 */
@Composable
fun HomeScreen(
    navController: NavHostController,
    musicViewModel: MusicViewModel,
    authViewModel: AuthViewModel
) {
    var selectedFilter by remember { mutableStateOf("Todas") }
    val artists by musicViewModel.artists.collectAsState()
    val albums by musicViewModel.albums.collectAsState()
    val songs by musicViewModel.songs.collectAsState()
    val userState by authViewModel.userState.collectAsState()

    Scaffold(
        containerColor = Color.Black,
        topBar = { 
            HomeTopBar(
                navController = navController,
                activeFilter = selectedFilter,
                profilePhotoUrl = userState.photoUrl,
                onFilterChange = { selectedFilter = it }
            ) 
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            if (songs.isEmpty() && albums.isEmpty()) {
                item {
                    Box(Modifier.fillParentMaxHeight(0.7f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF1DB954))
                    }
                }
            } else {
                when (selectedFilter) {
                    "Todas" -> {
                        item {
                            Row(Modifier.fillMaxWidth()) {
                                QuickAccessGridItem(
                                    title = "Tus me gustas",
                                    imageRes = R.drawable.corazon,
                                    modifier = Modifier.weight(1f),
                                    onClick = { navController.navigate(Screen.LikedSongs.route) }
                                )
                                Spacer(Modifier.width(8.dp))
                                
                                if (albums.isNotEmpty()) {
                                    val firstAlbum = albums.first()
                                    QuickAccessGridItem(
                                        title = firstAlbum.title,
                                        imageUrl = firstAlbum.coverUrl,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            musicViewModel.seleccionarAlbum(firstAlbum)
                                            navController.navigate(Screen.AlbumDetail.route)
                                        }
                                    )
                                }
                            }
                        }

                        if (songs.isNotEmpty()) {
                            item {
                                SectionTitle("Recomendado para ti")
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    items(songs) { song ->
                                        HorizontalSongCard(song = song) {
                                            musicViewModel.playSong(song, songs)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            SectionTitle("Tus mixes")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(albums) { album ->
                                    HorizontalMixCard(
                                        name = album.title, 
                                        imageUrl = album.coverUrl,
                                        onClick = {
                                            musicViewModel.seleccionarAlbum(album)
                                            navController.navigate(Screen.AlbumDetail.route)
                                        }
                                    )
                                }
                            }
                        }
                        
                        item {
                            SectionTitle("Tus artistas favoritos")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(artists) { artist ->
                                    CircularArtistItem(
                                        name = artist.name,
                                        imageUrl = artist.imageUrl,
                                        onClick = { navController.navigate(Screen.Library.route) }
                                    )
                                }
                            }
                        }
                    }

                    "Música" -> {
                        item { SectionTitle("Últimos lanzamientos") }
                        items(songs.take(5)) { song ->
                            DetailedReleaseCard(
                                song = song,
                                onClick = { musicViewModel.playSong(song, songs) },
                                onFavoriteClick = { musicViewModel.toggleFavorite(userState.uid, song.id) }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    "Podcasts" -> {
                        item { SectionTitle("Episodios más recientes") }
                        item { EmptyPodcastsView() }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
fun HomeTopBar(
    navController: NavHostController,
    activeFilter: String,
    profilePhotoUrl: String,
    onFilterChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
                    .clickable { navController.navigate(Screen.Panelusu.route) },
                contentAlignment = Alignment.Center
            ) {
                if (profilePhotoUrl.isNotEmpty()) {
                    AsyncImage(model = profilePhotoUrl, contentDescription = "Perfil", contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChipItem(text = "Todas", isActive = activeFilter == "Todas") { onFilterChange("Todas") }
                FilterChipItem(text = "Música", isActive = activeFilter == "Música") { onFilterChange("Música") }
                FilterChipItem(text = "Podcasts", isActive = activeFilter == "Podcasts") { onFilterChange("Podcasts") }
            }
        }
    }
}

@Composable
fun FilterChipItem(text: String, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = CircleShape,
        color = if (isActive) Color(0xFF1DB954) else Color(0xFF333333)
    ) {
        Text(
            text = text,
            color = if (isActive) Color.Black else Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DetailedReleaseCard(
    song: com.example.spotifyclone.model.Song,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(song.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(song.artist, color = Color.Gray, fontSize = 14.sp)
                    Text("Sencillo • Reciente", color = Color.Gray, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(color = Color.DarkGray.copy(alpha = 0.5f), shape = CircleShape) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shuffle, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Avance disponible", color = Color.White, fontSize = 11.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onFavoriteClick() }) {
                        Icon(Icons.Default.AddCircleOutline, "Agregar a favoritos", tint = Color.White)
                    }
                    IconButton(onClick = { onClick() }) {
                        Icon(Icons.Default.PlayCircle, "Reproducir", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalSongCard(song: com.example.spotifyclone.model.Song, onClick: () -> Unit) {
    Column(modifier = Modifier.width(140.dp).clickable { onClick() }) {
        AsyncImage(
            model = song.coverUrl,
            contentDescription = null,
            modifier = Modifier.size(140.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(song.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(song.artist, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
fun QuickAccessGridItem(
    title: String, 
    imageRes: Int? = null, 
    imageUrl: String? = null, 
    modifier: Modifier = Modifier, 
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(56.dp).clickable { onClick() },
        color = Color(0xFF333333),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (imageRes != null) {
                Image(painterResource(imageRes), null, Modifier.size(56.dp), contentScale = ContentScale.Crop)
            } else if (imageUrl != null) {
                AsyncImage(imageUrl, null, Modifier.size(56.dp), contentScale = ContentScale.Crop)
            }
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp), maxLines = 2)
        }
    }
}

@Composable
fun HorizontalMixCard(name: String, imageUrl: String, onClick: () -> Unit) {
    Column(modifier = Modifier.width(150.dp).clickable { onClick() }) {
        AsyncImage(imageUrl, null, Modifier.size(150.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Mix de $name", color = Color.LightGray, fontSize = 12.sp)
    }
}

@Composable
fun CircularArtistItem(name: String, imageUrl: String, onClick: () -> Unit = {}) {
    Column(modifier = Modifier.width(120.dp).clickable { onClick() }, horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(imageUrl, null, Modifier.size(120.dp).clip(CircleShape), contentScale = ContentScale.Crop)
        Spacer(modifier = Modifier.height(8.dp))
        Text(name, color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun EmptyPodcastsView() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), 
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFF282828)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Podcasts, null, tint = Color(0xFF1DB954), modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("Todavía no sigues ningún podcast", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("Busca tus programas favoritos y dale a 'Seguir'.", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { /* Navegar a búsqueda o categoría podcasts */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text("Explorar podcasts", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
