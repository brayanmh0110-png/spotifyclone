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
 * Organiza el contenido en 4 pestañas: Canciones, Álbumes, Artistas y Playlists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    navController: NavHostController,
    musicViewModel: MusicViewModel,
    authViewModel: AuthViewModel
) {
    val listaCanciones by musicViewModel.songs.collectAsState()
    val listaAlbumes by musicViewModel.albums.collectAsState()
    val listaArtistas by musicViewModel.artists.collectAsState()
    val listaPlaylists by musicViewModel.playlists.collectAsState()
    val estadoUsuario by authViewModel.userState.collectAsState()

    var tabSeleccionado by remember { mutableStateOf(0) }
    var mostrarCrearPlaylist by remember { mutableStateOf(false) }
    var nombreNuevaPlaylist by remember { mutableStateOf("") }
    var cancionParaAgregarAPlaylist by remember { mutableStateOf<Song?>(null) }

    // Cargamos las playlists del usuario cuando se conoce su UID
    LaunchedEffect(estadoUsuario.uid) {
        if (estadoUsuario.uid.isNotEmpty()) {
            musicViewModel.cargarPlaylists(estadoUsuario.uid)
        }
    }

    val nombreTabs = listOf("Canciones", "Álbumes", "Artistas", "Playlists")

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
                // Pestañas de navegación
                TabRow(
                    selectedTabIndex = tabSeleccionado,
                    containerColor = Color.Black,
                    contentColor = Color(0xFF1DB954)
                ) {
                    nombreTabs.forEachIndexed { indice, nombre ->
                        Tab(
                            selected = tabSeleccionado == indice,
                            onClick = { tabSeleccionado = indice },
                            text = {
                                Text(
                                    nombre,
                                    color = if (tabSeleccionado == indice) Color.White else Color.Gray,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }
            }
        },
        // Botón flotante para crear playlist (solo aparece en la pestaña Playlists)
        floatingActionButton = {
            if (tabSeleccionado == 3) {
                FloatingActionButton(
                    onClick = { mostrarCrearPlaylist = true },
                    containerColor = Color(0xFF1DB954)
                ) {
                    Icon(Icons.Default.Add, "Crear playlist", tint = Color.Black)
                }
            }
        }
    ) { rellenos ->
        Box(modifier = Modifier.padding(rellenos)) {
            when (tabSeleccionado) {
                0 -> TabCanciones(
                    canciones = listaCanciones,
                    vistaModelo = musicViewModel,
                    navController = navController,
                    onAgregarAPlaylist = { cancionParaAgregarAPlaylist = it }
                )
                1 -> TabAlbumes(
                    albumes = listaAlbumes,
                    onAlbumClick = { album ->
                        musicViewModel.seleccionarAlbum(album)
                        navController.navigate(Screen.AlbumDetail.route)
                    }
                )
                2 -> TabArtistas(artistas = listaArtistas)
                3 -> TabPlaylists(
                    playlists = listaPlaylists,
                    onPlaylistClick = { playlist ->
                        navController.navigate(Screen.PlaylistDetail.crearRuta(playlist.id))
                    },
                    onEliminarPlaylist = { playlist ->
                        musicViewModel.eliminarPlaylist(estadoUsuario.uid, playlist.id)
                    }
                )
            }
        }
    }

    // --- Diálogo: Crear nueva playlist ---
    if (mostrarCrearPlaylist) {
        AlertDialog(
            onDismissRequest = {
                mostrarCrearPlaylist = false
                nombreNuevaPlaylist = ""
            },
            title = { Text("Nueva playlist", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = nombreNuevaPlaylist,
                    onValueChange = { nombreNuevaPlaylist = it },
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
                        if (nombreNuevaPlaylist.isNotBlank()) {
                            musicViewModel.crearPlaylist(estadoUsuario.uid, nombreNuevaPlaylist.trim())
                            mostrarCrearPlaylist = false
                            nombreNuevaPlaylist = ""
                        }
                    }
                ) {
                    Text("Crear", color = Color(0xFF1DB954))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarCrearPlaylist = false
                    nombreNuevaPlaylist = ""
                }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF282828)
        )
    }

    // --- Diálogo: Seleccionar playlist a la que agregar una canción ---
    cancionParaAgregarAPlaylist?.let { cancion ->
        AlertDialog(
            onDismissRequest = { cancionParaAgregarAPlaylist = null },
            title = { Text("Agregar a playlist", color = Color.White) },
            text = {
                if (listaPlaylists.isEmpty()) {
                    Text(
                        "No tienes playlists aún.\nVe a la pestaña Playlists y pulsa +.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn {
                        items(listaPlaylists) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        musicViewModel.agregarCancionAPlaylist(
                                            estadoUsuario.uid,
                                            playlist.id,
                                            cancion.id
                                        )
                                        cancionParaAgregarAPlaylist = null
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
                TextButton(onClick = { cancionParaAgregarAPlaylist = null }) {
                    Text("Cerrar", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF282828)
        )
    }
}

// --- PESTAÑA: Canciones ---

@Composable
private fun TabCanciones(
    canciones: List<Song>,
    vistaModelo: MusicViewModel,
    navController: NavHostController,
    onAgregarAPlaylist: (Song) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Item especial: Tus me gustas
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

        item {
            Text(
                text = "${canciones.size} canciones",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        items(canciones) { cancion ->
            ItemCancionBiblioteca(
                cancion = cancion,
                alPulsar = { vistaModelo.playSong(cancion, canciones) },
                alAgregarACola = { vistaModelo.agregarALaCola(cancion) },
                alAgregarAPlaylist = { onAgregarAPlaylist(cancion) }
            )
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// --- PESTAÑA: Álbumes ---

@Composable
private fun TabAlbumes(albumes: List<Album>, onAlbumClick: (Album) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                "${albumes.size} álbumes",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        items(albumes) { album ->
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
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(album.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                    Text("Álbum • ${album.artist}", color = Color.Gray, fontSize = 14.sp, maxLines = 1)
                }
                Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

// --- PESTAÑA: Artistas ---

@Composable
private fun TabArtistas(artistas: List<Artist>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                "${artistas.size} artistas",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        items(artistas) { artista ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = artista.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(artista.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text("Artista", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

// --- PESTAÑA: Playlists ---

@Composable
private fun TabPlaylists(
    playlists: List<Playlist>, 
    onPlaylistClick: (Playlist) -> Unit,
    onEliminarPlaylist: (Playlist) -> Unit
) {
    if (playlists.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No tienes playlists aún.\nPulsa el botón + para crear una.",
                color = Color.Gray,
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
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
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF282828)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.QueueMusic, null, tint = Color(0xFF1DB954), modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(playlist.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text("${playlist.songsIds.size} canciones", color = Color.Gray, fontSize = 14.sp)
                    }
                    
                    // Menú para eliminar playlist
                    var menuPlaylist by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { menuPlaylist = true }) {
                            Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
                        }
                        DropdownMenu(
                            expanded = menuPlaylist,
                            onDismissRequest = { menuPlaylist = false },
                            containerColor = Color(0xFF282828)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Eliminar Playlist", color = Color.Red) },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) },
                                onClick = {
                                    onEliminarPlaylist(playlist)
                                    menuPlaylist = false
                                }
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// --- COMPONENTE REUTILIZABLE: Fila de canción con menú de opciones ---

@Composable
fun ItemCancionBiblioteca(
    cancion: Song,
    alPulsar: () -> Unit,
    alAgregarACola: () -> Unit = {},
    alAgregarAPlaylist: () -> Unit = {}
) {
    var menuExpandido by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { alPulsar() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = cancion.coverUrl,
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(cancion.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Text("Canción • ${cancion.artist}", color = Color.Gray, fontSize = 14.sp, maxLines = 1)
        }

        // Menú de opciones con DropdownMenu
        Box {
            IconButton(onClick = { menuExpandido = true }) {
                Icon(Icons.Default.MoreVert, "Opciones", tint = Color.Gray)
            }
            DropdownMenu(
                expanded = menuExpandido,
                onDismissRequest = { menuExpandido = false },
                containerColor = Color(0xFF282828)
            ) {
                DropdownMenuItem(
                    text = { Text("Agregar a la cola", color = Color.White) },
                    leadingIcon = { Icon(Icons.Default.QueueMusic, null, tint = Color.White) },
                    onClick = {
                        alAgregarACola()
                        menuExpandido = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Agregar a playlist", color = Color.White) },
                    leadingIcon = { Icon(Icons.Default.PlaylistAdd, null, tint = Color.White) },
                    onClick = {
                        alAgregarAPlaylist()
                        menuExpandido = false
                    }
                )
            }
        }
    }
}
