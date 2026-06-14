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
 * Permite reproducirlas todas en cadena o individualmente.
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
                IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                    Icon(Icons.Default.Search, "Buscar", tint = Color.White)
                }
            }
        }
    ) { rellenos ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(rellenos)) {
            // Cabecera con degradado
            item {
                CabeceraFavoritos(
                    cantidad = listaFavoritos.size,
                    alReproducirTodo = {
                        listaFavoritos.firstOrNull()?.let { musicViewModel.playSong(it, listaFavoritos) }
                    }
                )
            }

            // Si no hay favoritos, mostramos el "Empty State"
            if (listaFavoritos.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 80.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FavoriteBorder, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("Aún no tienes canciones favoritas", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Tus canciones favoritas aparecerán aquí.", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                // Lista de favoritos cargada de Firebase
                items(listaFavoritos) { cancion ->
                    ItemCancionFavorita(
                        cancion = cancion,
                        alPulsar = { 
                            musicViewModel.playSong(cancion, listaFavoritos)
                            musicViewModel.registrarReproduccion(estadoUsuario.uid, cancion)
                        },
                        alQuitar = { musicViewModel.toggleFavorite(estadoUsuario.uid, cancion) },
                        alAgregarACola = { musicViewModel.agregarALaCola(cancion) },
                        alAgregarAPlaylist = { cancionParaPlaylist = cancion }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

    // Diálogo para mover de "Me gusta" a una Playlist
    if (cancionParaPlaylist != null) {
        PlaylistSelectionDialog(
            playlists = listaPlaylists,
            onPlaylistSelect = { pl ->
                musicViewModel.agregarCancionAPlaylist(estadoUsuario.uid, pl.id, cancionParaPlaylist!!.id)
                cancionParaPlaylist = null
                Toast.makeText(context, "Añadida", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { cancionParaPlaylist = null }
        )
    }
}

@Composable
fun CabeceraFavoritos(cantidad: Int, alReproducirTodo: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(brush = Brush.verticalGradient(colors = listOf(Color(0xFF503691), Color.Black)))
            .padding(16.dp)
    ) {
        Text("Tus me gustas", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("$cantidad canciones", color = Color.LightGray, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.align(Alignment.CenterEnd).size(56.dp).clip(CircleShape).background(Color(0xFF1DB954)).clickable { alReproducirTodo() },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(32.dp)) }
        }
    }
}

@Composable
fun ItemCancionFavorita(cancion: MusicSong, alPulsar: () -> Unit, alQuitar: () -> Unit, alAgregarACola: () -> Unit, alAgregarAPlaylist: () -> Unit) {
    var menuExpandido by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().clickable { alPulsar() }.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(cancion.coverUrl, null, Modifier.size(50.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(cancion.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(cancion.artist, color = Color.Gray, fontSize = 14.sp)
        }
        IconButton(onClick = alQuitar) { Icon(Icons.Default.Favorite, null, tint = Color(0xFF1DB954)) }
        Box {
            IconButton(onClick = { menuExpandido = true }) { Icon(Icons.Default.MoreVert, null, tint = Color.Gray) }
            DropdownMenu(expanded = menuExpandido, onDismissRequest = { menuExpandido = false }, containerColor = Color(0xFF282828)) {
                DropdownMenuItem(text = { Text("Agregar a la cola", color = Color.White) }, onClick = { alAgregarACola(); menuExpandido = false })
                DropdownMenuItem(text = { Text("Agregar a playlist", color = Color.White) }, onClick = { alAgregarAPlaylist(); menuExpandido = false })
            }
        }
    }
}
