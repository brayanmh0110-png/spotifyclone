package com.example.spotifyclone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.spotifyclone.model.Album
import com.example.spotifyclone.model.Artist
import com.example.spotifyclone.model.Playlist
import com.example.spotifyclone.model.Song
import com.example.spotifyclone.navigation.Screen
import com.example.spotifyclone.viewmodel.AuthViewModel
import com.example.spotifyclone.viewmodel.MusicViewModel

/**
 * LibraryScreen: Biblioteca personal del usuario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    navController: NavHostController,
    musicViewModel: MusicViewModel,
    authViewModel: AuthViewModel
) {
    val songs by musicViewModel.songs.collectAsState()
    val albums by musicViewModel.albums.collectAsState()
    val artists by musicViewModel.artists.collectAsState()
    val playlists by musicViewModel.playlists.collectAsState()
    val userState by authViewModel.userState.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var songToAddtoPlaylist by remember { mutableStateOf<Song?>(null) }

    LaunchedEffect(userState.uid) {
        if (userState.uid.isNotEmpty()) {
            musicViewModel.cargarPlaylists(userState.uid)
        }
    }

    val tabNames = listOf("Canciones", "Álbumes", "Artistas", "Playlists")

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text("Tu Biblioteca", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
                )
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Black,
                    contentColor = Color(0xFF1DB954)
                ) {
                    tabNames.forEachIndexed { index, name ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    name,
                                    color = if (selectedTabIndex == index) Color.White else Color.Gray,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedTabIndex == 3) {
                FloatingActionButton(
                    onClick = { showCreatePlaylistDialog = true },
                    containerColor = Color(0xFF1DB954)
                ) {
                    Icon(Icons.Default.Add, "Crear playlist", tint = Color.Black)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTabIndex) {
                0 -> SongsTab(
                    songs = songs,
                    musicViewModel = musicViewModel,
                    navController = navController,
                    onAddToPlaylist = { songToAddtoPlaylist = it }
                )
                1 -> AlbumsTab(
                    albums = albums,
                    onAlbumClick = { album ->
                        musicViewModel.seleccionarAlbum(album)
                        navController.navigate(Screen.AlbumDetail.route)
                    }
                )
                2 -> ArtistsTab(artists = artists)
                3 -> PlaylistsTab(
                    playlists = playlists,
                    onPlaylistClick = { playlist ->
                        navController.navigate(Screen.PlaylistDetail.crearRuta(playlist.id))
                    },
                    onDeletePlaylist = { playlist ->
                        musicViewModel.eliminarPlaylist(userState.uid, playlist.id)
                    }
                )
            }
        }
    }

    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreatePlaylistDialog = false
                newPlaylistName = ""
            },
            title = { Text("Nueva playlist", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Nombre de la playlist") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            musicViewModel.crearPlaylist(userState.uid, newPlaylistName.trim())
                            showCreatePlaylistDialog = false
                            newPlaylistName = ""
                        }
                    }
                ) {
                    Text("Crear", color = Color(0xFF1DB954))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreatePlaylistDialog = false
                    newPlaylistName = ""
                }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF282828)
        )
    }

    songToAddtoPlaylist?.let { song ->
        AlertDialog(
            onDismissRequest = { songToAddtoPlaylist = null },
            title = { Text("Agregar a playlist", color = Color.White) },
            text = {
                if (playlists.isEmpty()) {
                    Text("No tienes playlists aún.", color = Color.Gray)
                } else {
                    LazyColumn {
                        items(playlists) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        musicViewModel.agregarCancionAPlaylist(userState.uid, playlist.id, song.id)
                                        songToAddtoPlaylist = null
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.QueueMusic, null, tint = Color(0xFF1DB954))
                                Spacer(Modifier.width(12.dp))
                                Text(playlist.name, color = Color.White, fontSize = 16.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { songToAddtoPlaylist = null }) {
                    Text("Cerrar", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF282828)
        )
    }
}

@Composable
private fun SongsTab(
    songs: List<Song>,
    musicViewModel: MusicViewModel,
    navController: NavHostController,
    onAddToPlaylist: (Song) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(Screen.LikedSongs.route) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush = Brush.verticalGradient(colors = listOf(Color(0xFF503691), Color.Black))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Favorite, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Tus me gustas", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Playlist • Lista de favoritos", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }

        items(songs) { song ->
            LibrarySongItem(
                song = song,
                onPlay = { musicViewModel.playSong(song, songs) },
                onAddToQueue = { musicViewModel.agregarALaCola(song) },
                onAddToPlaylist = { onAddToPlaylist(song) }
            )
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun AlbumsTab(albums: List<Album>, onAlbumClick: (Album) -> Unit) {
    if (albums.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay álbumes disponibles", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(albums) { album ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAlbumClick(album) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = album.coverUrl,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(album.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                        Text("Álbum • ${album.artist}", color = Color.Gray, fontSize = 14.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistsTab(artists: List<Artist>) {
    if (artists.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay artistas disponibles", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(artists) { artist ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = artist.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(artist.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text("Artista", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistsTab(
    playlists: List<Playlist>, 
    onPlaylistClick: (Playlist) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit
) {
    if (playlists.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.LibraryMusic, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("Crea tu primera playlist", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Es muy fácil, te ayudaremos.", color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            // El FAB ya está para crear, pero podemos poner un texto informativo
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(playlists) { playlist ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlaylistClick(playlist) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF282828)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.QueueMusic, null, tint = Color(0xFF1DB954), modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(playlist.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text("${playlist.songsIds.size} canciones", color = Color.Gray, fontSize = 14.sp)
                    }
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            containerColor = Color(0xFF282828)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Eliminar Playlist", color = Color.Red) },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) },
                                onClick = {
                                    onDeletePlaylist(playlist)
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LibrarySongItem(
    song: Song,
    onPlay: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onPlay() }.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.coverUrl,
            contentDescription = null,
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(song.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Text("Canción • ${song.artist}", color = Color.Gray, fontSize = 14.sp, maxLines = 1)
        }
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, "Opciones", tint = Color.Gray)
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                containerColor = Color(0xFF282828)
            ) {
                DropdownMenuItem(
                    text = { Text("Agregar a la cola", color = Color.White) },
                    leadingIcon = { Icon(Icons.Default.QueueMusic, null, tint = Color.White) },
                    onClick = {
                        onAddToQueue()
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Agregar a playlist", color = Color.White) },
                    leadingIcon = { Icon(Icons.Default.PlaylistAdd, null, tint = Color.White) },
                    onClick = {
                        onAddToPlaylist()
                        showMenu = false
                    }
                )
            }
        }
    }
}
