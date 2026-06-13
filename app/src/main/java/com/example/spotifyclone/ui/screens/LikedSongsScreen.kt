package com.example.spotifyclone.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.spotifyclone.R
import com.example.spotifyclone.navigation.Screen
import com.example.spotifyclone.viewmodel.AuthViewModel
import com.example.spotifyclone.viewmodel.MusicViewModel
import com.example.spotifyclone.model.Song as MusicSong

/**
 * LikedSongsScreen: Pantalla que muestra las canciones marcadas como "Favoritas".
 */
@Composable
fun LikedSongsScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    musicViewModel: MusicViewModel
) {
    val context = LocalContext.current
    val listaFavoritos by musicViewModel.favorites.collectAsState()
    val estadoUsuario by authViewModel.userState.collectAsState()
    val listaPlaylists by musicViewModel.playlists.collectAsState()

    // Canción seleccionada para agregar a playlist
    var cancionParaPlaylist by remember { mutableStateOf<MusicSong?>(null) }

    LaunchedEffect(estadoUsuario.uid) {
        if (estadoUsuario.uid.isNotEmpty()) {
            musicViewModel.loadFavorites(estadoUsuario.uid)
            musicViewModel.cargarPlaylists(estadoUsuario.uid)
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                // Botón buscar → va a la pantalla de búsqueda
                IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                    Icon(Icons.Default.Search, "Buscar", tint = Color.White)
                }
                IconButton(onClick = {
                    Toast.makeText(context, "Sin opciones adicionales", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.MoreVert, "Opciones", tint = Color.White)
                }
            }
        }
    ) { rellenos ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(rellenos)) {
            item {
                CabeceraFavoritos(
                    cantidad = listaFavoritos.size,
                    alReproducirTodo = {
                        listaFavoritos.firstOrNull()?.let {
                            musicViewModel.playSong(it, listaFavoritos)
                        }
                    }
                )
            }

            if (listaFavoritos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FavoriteBorder, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("Aún no tienes canciones favoritas", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Tus canciones favoritas aparecerán aquí.", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(listaFavoritos) { cancion ->
                    ItemCancionFavorita(
                        cancion = cancion,
                        alPulsar = { musicViewModel.playSong(cancion, listaFavoritos) },
                        alQuitar = { musicViewModel.toggleFavorite(estadoUsuario.uid, cancion.id) },
                        alAgregarACola = { musicViewModel.agregarALaCola(cancion) },
                        alAgregarAPlaylist = { cancionParaPlaylist = cancion }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

    // Diálogo para seleccionar a qué playlist agregar la canción
    cancionParaPlaylist?.let { cancion ->
        AlertDialog(
            onDismissRequest = { cancionParaPlaylist = null },
            title = { Text("Agregar a playlist", color = Color.White) },
            text = {
                if (listaPlaylists.isEmpty()) {
                    Text(
                        "No tienes playlists aún.\nCrea una desde Tu Biblioteca.",
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
                                            estadoUsuario.uid, playlist.id, cancion.id
                                        )
                                        cancionParaPlaylist = null
                                        Toast.makeText(context, "Canción agregada a \"${playlist.name}\"", Toast.LENGTH_SHORT).show()
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
                TextButton(onClick = { cancionParaPlaylist = null }) {
                    Text("Cerrar", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF282828)
        )
    }
}

@Composable
fun CabeceraFavoritos(cantidad: Int, alReproducirTodo: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = Brush.verticalGradient(colors = listOf(Color(0xFF503691), Color.Black)))
            .padding(16.dp)
    ) {
        Text("Tus me gustas", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("$cantidad canciones", color = Color.LightGray, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            // Botón Play ahora funcional
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1DB954))
                    .clickable { alReproducirTodo() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, "Reproducir todas", tint = Color.Black, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun ItemCancionFavorita(
    cancion: MusicSong,
    alPulsar: () -> Unit,
    alQuitar: () -> Unit,
    alAgregarACola: () -> Unit = {},
    alAgregarAPlaylist: () -> Unit = {}
) {
    var menuExpandido by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().clickable { alPulsar() }.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (cancion.coverUrl.isNotEmpty()) {
            AsyncImage(cancion.coverUrl, null, Modifier.size(50.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
        } else {
            Image(painterResource(R.drawable.corazon), null, Modifier.size(50.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(cancion.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(cancion.artist, color = Color.Gray, fontSize = 14.sp)
        }
        // Botón corazón para quitar de favoritos
        IconButton(onClick = { alQuitar() }) {
            Icon(Icons.Default.Favorite, "Quitar de favoritos", tint = Color(0xFF1DB954))
        }
        // Menú de opciones
        Box {
            IconButton(onClick = { menuExpandido = true }) {
                Icon(Icons.Default.MoreVert, "Opciones", tint = Color.Gray)
            }
            DropdownMenu(expanded = menuExpandido, onDismissRequest = { menuExpandido = false }) {
                DropdownMenuItem(
                    text = { Text("Agregar a la cola") },
                    leadingIcon = { Icon(Icons.Default.QueueMusic, null) },
                    onClick = { alAgregarACola(); menuExpandido = false }
                )
                DropdownMenuItem(
                    text = { Text("Agregar a playlist") },
                    leadingIcon = { Icon(Icons.Default.PlaylistAdd, null) },
                    onClick = { alAgregarAPlaylist(); menuExpandido = false }
                )
            }
        }
    }
}
