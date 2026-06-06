package com.example.spotifyclone.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.spotifyclone.model.Album
import com.example.spotifyclone.model.Song
import com.example.spotifyclone.viewmodel.AuthViewModel
import com.example.spotifyclone.viewmodel.MusicViewModel

/**
 * AlbumDetailScreen: Pantalla que muestra el contenido de un álbum específico.
 * Ahora recibe AuthViewModel para poder agregar canciones a playlists.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AlbumDetailScreen(
    controladorNavegacion: NavHostController,
    vistaModeloMusica: MusicViewModel,
    vistaModeloAutenticacion: AuthViewModel
) {
    val context = LocalContext.current
    val listaCancionesAlbum by vistaModeloMusica.albumSongs.collectAsState()
    val albumActual by vistaModeloMusica.albumActual.collectAsState()
    val estadoUsuario by vistaModeloAutenticacion.userState.collectAsState()
    val listaPlaylists by vistaModeloMusica.playlists.collectAsState()

    val album = albumActual ?: Album(title = "Cargando...", artist = "...", coverUrl = "")

    // Canción seleccionada para agregar a playlist (null = diálogo cerrado)
    var cancionParaPlaylist by remember { mutableStateOf<Song?>(null) }

    // Cargamos las playlists del usuario si aún no se han cargado
    LaunchedEffect(estadoUsuario.uid) {
        if (estadoUsuario.uid.isNotEmpty()) {
            vistaModeloMusica.cargarPlaylists(estadoUsuario.uid)
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { controladorNavegacion.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { rellenos ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(rellenos)) {
            item { CabeceraImagenAlbum(album.coverUrl) }
            item { InformacionTextoAlbum(album) }
            item {
                PanelAccionesAlbum(
                    alReproducirTodo = {
                        listaCancionesAlbum.firstOrNull()?.let {
                            vistaModeloMusica.playSong(it, listaCancionesAlbum)
                        }
                    },
                    alAlternarAleatorio = {
                        vistaModeloMusica.alternarAleatorio()
                        listaCancionesAlbum.randomOrNull()?.let {
                            vistaModeloMusica.playSong(it, listaCancionesAlbum)
                        }
                    },
                    alAgregarABiblioteca = {
                        Toast.makeText(context, "Álbum guardado en tu biblioteca", Toast.LENGTH_SHORT).show()
                    },
                    alDescargar = {
                        Toast.makeText(context, "Descarga no disponible en esta versión", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            items(listaCancionesAlbum) { cancion ->
                FilaCancionAlbum(
                    cancion = cancion,
                    alPulsar = { vistaModeloMusica.playSong(cancion, listaCancionesAlbum) },
                    alAgregarACola = { vistaModeloMusica.agregarALaCola(cancion) },
                    alAgregarAPlaylist = { cancionParaPlaylist = cancion }
                )
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

    // Diálogo para seleccionar playlist a la que agregar la canción
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
                                        vistaModeloMusica.agregarCancionAPlaylist(
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
fun CabeceraImagenAlbum(urlPortada: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(brush = Brush.verticalGradient(colors = listOf(Color.Gray.copy(alpha = 0.5f), Color.Black))),
        contentAlignment = Alignment.Center
    ) {
        Surface(modifier = Modifier.size(200.dp), shadowElevation = 12.dp, color = Color.DarkGray) {
            if (urlPortada.isNotEmpty()) {
                AsyncImage(model = urlPortada, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Box(Modifier.fillMaxSize().background(Color.DarkGray)) {
                    Icon(Icons.Default.Audiotrack, null, Modifier.align(Alignment.Center).size(80.dp), tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun InformacionTextoAlbum(album: Album) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(album.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.Gray))
            Spacer(modifier = Modifier.width(8.dp))
            Text(album.artist, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Álbum • ${album.releaseDate}", color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
fun PanelAccionesAlbum(
    alReproducirTodo: () -> Unit = {},
    alAlternarAleatorio: () -> Unit = {},
    alAgregarABiblioteca: () -> Unit = {},
    alDescargar: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Agregar a biblioteca
        IconButton(onClick = { alAgregarABiblioteca() }) {
            Icon(Icons.Default.AddCircleOutline, "Guardar en biblioteca", tint = Color.Gray, modifier = Modifier.size(28.dp))
        }
        // Descargar
        IconButton(onClick = { alDescargar() }) {
            Icon(Icons.Default.DownloadForOffline, "Descargar", tint = Color.Gray, modifier = Modifier.size(28.dp))
        }

        Spacer(Modifier.weight(1f))

        // Botón Shuffle
        IconButton(onClick = { alAlternarAleatorio() }) {
            Icon(Icons.Default.Shuffle, "Reproducción aleatoria", tint = Color(0xFF1DB954), modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.width(8.dp))
        // Botón Play circular
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFF1DB954)).clickable { alReproducirTodo() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, "Reproducir todo", tint = Color.Black, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun FilaCancionAlbum(
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
        Column(modifier = Modifier.weight(1f)) {
            Text(cancion.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(cancion.artist, color = Color.Gray, fontSize = 12.sp)
        }
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
                    onClick = { alAgregarACola(); menuExpandido = false }
                )
                DropdownMenuItem(
                    text = { Text("Agregar a playlist", color = Color.White) },
                    leadingIcon = { Icon(Icons.Default.PlaylistAdd, null, tint = Color.White) },
                    onClick = { alAgregarAPlaylist(); menuExpandido = false }
                )
            }
        }
    }
}
