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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.spotifyclone.navigation.Screen
import com.example.spotifyclone.R
import coil.compose.AsyncImage
import com.example.spotifyclone.viewmodel.MusicViewModel

@Composable
fun HomeScreen(navController: NavHostController, musicViewModel: MusicViewModel) {
    var selectedFilter by remember { mutableStateOf("Todas") }
    val artists by musicViewModel.artists.collectAsState()
    val albums by musicViewModel.albums.collectAsState()
    val songs by musicViewModel.songs.collectAsState()

    Scaffold(
        containerColor = Color.Black,
        topBar = { HomeTopBar(navController, selectedFilter) { selectedFilter = it } },
        bottomBar = { HomeBottomBar(navController) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            // Grid de Tarjetas (Sección superior)
            item {
                Column {
                    Row(Modifier.fillMaxWidth()) {
                        HomeGridItem(
                            "Tus me gustas",
                            R.drawable.corazon,
                            Modifier.weight(1f),
                            onClick = { navController.navigate(Screen.LikedSongs.route) }
                        )
                        Spacer(Modifier.width(8.dp))
                        
                        if (albums.isNotEmpty()) {
                            val album = albums.first()
                            HomeGridItem(
                                album.title,
                                album.coverUrl,
                                Modifier.weight(1f),
                                onClick = { 
                                    musicViewModel.loadSongsByAlbum(album.songIds)
                                    navController.navigate(Screen.AlbumDetail.route) 
                                }
                            )
                        } else {
                            // Placeholder mientras carga
                            Box(Modifier.weight(1f).height(56.dp).background(Color(0xFF333333)))
                        }
                    }
                }
            }

            // SECCIÓN: Recomendado para ti (Firestore)
            if (songs.isNotEmpty()) {
                item {
                    SectionTitle("Recomendado para ti")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(songs) { song ->
                            SongItem(song = song) {
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
                        MixItem(
                            name = album.title, 
                            imageUrl = album.coverUrl,
                            onClick = {
                                musicViewModel.loadSongsByAlbum(album.songIds)
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
                        ArtistItem(artist.name, artist.imageUrl)
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun SongItem(song: com.example.spotifyclone.model.Song, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = song.coverUrl,
            contentDescription = song.title,
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = song.title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            color = Color.Gray,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}

@Composable
fun HomeTopBar(
    navController: NavHostController,
    selectedFilter: String,
    onFilterClick: (String) -> Unit
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
                    .size(35.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .clickable{
                        navController.navigate(Screen.Panelusu.route)
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.perfil),
                    contentDescription = "Perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip("Todas", selectedFilter == "Todas") { onFilterClick("Todas") }
                FilterChip("Música", selectedFilter == "Música") { onFilterClick("Música") }
                FilterChip("Podcasts", selectedFilter == "Podcasts") { onFilterClick("Podcasts") }
                if (selectedFilter == "Música") {
                    FilterChip("Siguiendo", false) {}
                }
            }
        }
    }
}

@Composable
fun FilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = CircleShape,
        color = if (isSelected) Color(0xFF1DB954) else Color(0xFF333333)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HomeGridItem(title: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit = {}) {
    HomeGridItemContent(title, modifier, onClick) {
        Icon(icon, contentDescription = null, tint = Color.White)
    }
}

@Composable
fun HomeGridItem(title: String, imageUrl: String, modifier: Modifier, onClick: () -> Unit = {}) {
    HomeGridItemContent(title, modifier, onClick) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun HomeGridItem(title: String, imageRes: Int, modifier: Modifier, onClick: () -> Unit = {}) {
    HomeGridItemContent(title, modifier, onClick) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun HomeGridItemContent(
    title: String,
    modifier: Modifier,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .clickable { onClick() },
        color = Color(0xFF333333),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(56.dp)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp),
                maxLines = 2
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
fun MixItem(name: String, imageUrl: String, onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Mix de $name",
            color = Color.LightGray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun MixItem(name: String, imageRes: Int) {
    Column(modifier = Modifier.width(150.dp)) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Mix de $name",
            color = Color.LightGray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun ArtistItem(name: String, imageUrl: String) {
    Column(
        modifier = Modifier.width(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = name, color = Color.White, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun ArtistItem(name: String, imageRes: Int) {
    Column(
        modifier = Modifier.width(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = name, color = Color.White, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun HomeBottomBar(navController: NavHostController) {
    NavigationBar(containerColor = Color.Black.copy(alpha = 0.9f)) {
        val items = listOf(
            Triple("Inicio", Icons.Default.Home, Screen.Home.route),
            Triple("Buscar", Icons.Default.Search, Screen.Search.route),
            Triple("Biblioteca", Icons.Default.LibraryMusic, Screen.Library.route),
            Triple("Premium", Icons.Default.WorkspacePremium, ""),
            Triple("Crear", Icons.Default.AddCircle, "")
        )
        
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { (label, icon, route) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = {
                    if (route.isNotEmpty() && currentRoute != route) {
                        navController.navigate(route)
                    }
                },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = Color.White,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
