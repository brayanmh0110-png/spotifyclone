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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.spotifyclone.navigation.Screen
import com.example.spotifyclone.R
import coil.compose.AsyncImage
import com.example.spotifyclone.viewmodel.MusicViewModel
import com.example.spotifyclone.viewmodel.AuthViewModel
import com.example.spotifyclone.ui.components.SpotifyBottomBar

/**
 * HomeScreen: Pantalla principal de la aplicación.
 * Organiza el contenido en tres filtros: Todas, Música y Podcasts.
 */
@Composable
fun HomeScreen(
    controladorNavegacion: NavHostController,
    vistaModeloMusica: MusicViewModel,
    vistaModeloAutenticacion: AuthViewModel
) {
    // 1. Cargamos los datos necesarios desde los ViewModels
    var filtroSeleccionado by remember { mutableStateOf("Todas") }
    val listaArtistas by vistaModeloMusica.artists.collectAsState()
    val listaAlbumes by vistaModeloMusica.albums.collectAsState()
    val listaCanciones by vistaModeloMusica.songs.collectAsState()
    val datosUsuario by vistaModeloAutenticacion.userState.collectAsState()

    Scaffold(
        containerColor = Color.Black,
        // Barra superior con foto de perfil y filtros
        topBar = { 
            BarraSuperiorInicio(
                navController = controladorNavegacion,
                filtroActivo = filtroSeleccionado,
                urlFotoPerfil = datosUsuario.photoUrl,
                alCambiarFiltro = { filtroSeleccionado = it }
            ) 
        }
    ) { rellenos ->
        // Lista principal con scroll vertical
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(rellenos)
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            // Lógica para mostrar contenido según el filtro elegido
            when (filtroSeleccionado) {
                "Todas" -> {
                    // SECCIÓN: Accesos directos (Me gustas y primer álbum)
                    item {
                        Row(Modifier.fillMaxWidth()) {
                            ItemGridAccesoRapido(
                                titulo = "Tus me gustas",
                                recursoImagen = R.drawable.corazon,
                                modifier = Modifier.weight(1f),
                                alPulsar = { controladorNavegacion.navigate(Screen.LikedSongs.route) }
                            )
                            Spacer(Modifier.width(8.dp))
                            
                            if (listaAlbumes.isNotEmpty()) {
                                val primerAlbum = listaAlbumes.first()
                                ItemGridAccesoRapido(
                                    titulo = primerAlbum.title,
                                    urlImagen = primerAlbum.coverUrl,
                                    modifier = Modifier.weight(1f),
                                    alPulsar = {
                                        vistaModeloMusica.seleccionarAlbum(primerAlbum)
                                        controladorNavegacion.navigate(Screen.AlbumDetail.route)
                                    }
                                )
                            }
                        }
                    }

                    // SECCIÓN: Canciones recomendadas (Horizontal)
                    if (listaCanciones.isNotEmpty()) {
                        item {
                            TituloSeccion("Recomendado para ti")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(listaCanciones) { cancion ->
                                    TarjetaCancionHorizontal(cancion = cancion) {
                                        vistaModeloMusica.playSong(cancion, listaCanciones)
                                    }
                                }
                            }
                        }
                    }

                    // SECCIÓN: Mixes (Horizontal)//
                    item {
                        TituloSeccion("Tus mixes")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(listaAlbumes) { album ->
                                TarjetaMixHorizontal(
                                    nombre = album.title, 
                                    urlImagen = album.coverUrl,
                                    alPulsar = {
                                        vistaModeloMusica.seleccionarAlbum(album)
                                        controladorNavegacion.navigate(Screen.AlbumDetail.route)
                                    }
                                )
                            }
                        }
                    }
                    
                    // SECCIÓN: Artistas (Iconos circulares)
                    item {
                        TituloSeccion("Tus artistas favoritos")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(listaArtistas) { artista ->
                                ItemArtistaCircular(
                                    nombre = artista.name,
                                    urlImagen = artista.imageUrl,
                                    alPulsar = { controladorNavegacion.navigate(Screen.Library.route) }
                                )
                            }
                        }
                    }
                }

                "Música" -> {
                    item { TituloSeccion("Últimos lanzamientos") }
                    items(listaCanciones.take(5)) { cancion ->
                        TarjetaLanzamientoDetallada(
                            cancion = cancion,
                            alPulsar = { vistaModeloMusica.playSong(cancion, listaCanciones) },
                            alAgregarAFavoritos = { vistaModeloMusica.toggleFavorite(datosUsuario.uid, cancion.id) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                "Podcasts" -> {
                    item { TituloSeccion("Episodios más recientes") }
                    item { VistaPodcastsVacia() }
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// --- COMPONENTES INTERNOS (UI ATOMS) ---

@Composable
fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        color = Color.White,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
fun BarraSuperiorInicio(
    navController: NavHostController,
    filtroActivo: String,
    urlFotoPerfil: String,
    alCambiarFiltro: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icono de perfil dinámico
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
                    .clickable { navController.navigate(Screen.Panelusu.route) },
                contentAlignment = Alignment.Center
            ) {
                if (urlFotoPerfil.isNotEmpty()) {
                    AsyncImage(model = urlFotoPerfil, contentDescription = "Perfil", contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Fila de Filtros (Chips)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BotonFiltro(texto = "Todas", estaActivo = filtroActivo == "Todas") { alCambiarFiltro("Todas") }
                
                // Grupo Música
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    BotonFiltro(texto = "Música", estaActivo = filtroActivo == "Música") { alCambiarFiltro("Música") }
                    if (filtroActivo == "Música") BotonFiltro(texto = "Siguiendo", estaActivo = false) {}
                }

                // Grupo Podcasts
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    BotonFiltro(texto = "Podcasts", estaActivo = filtroActivo == "Podcasts") { alCambiarFiltro("Podcasts") }
                    if (filtroActivo == "Podcasts") BotonFiltro(texto = "Siguiendo", estaActivo = false) {}
                }

            }
        }
    }
}

@Composable
fun BotonFiltro(texto: String, estaActivo: Boolean, alPulsar: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { alPulsar() },
        shape = CircleShape,
        color = if (estaActivo) Color(0xFF1DB954) else Color(0xFF333333)
    ) {
        Text(
            text = texto,
            color = if (estaActivo) Color.Black else Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TarjetaLanzamientoDetallada(
    cancion: com.example.spotifyclone.model.Song,
    alPulsar: () -> Unit,
    alAgregarAFavoritos: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { alPulsar() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = cancion.coverUrl,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(cancion.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(cancion.artist, color = Color.Gray, fontSize = 14.sp)
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
                // Botones de acción ahora funcionales
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { alAgregarAFavoritos() }) {
                        Icon(Icons.Default.AddCircleOutline, "Agregar a favoritos", tint = Color.White)
                    }
                    IconButton(onClick = { alPulsar() }) {
                        Icon(Icons.Default.PlayCircle, "Reproducir", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaCancionHorizontal(cancion: com.example.spotifyclone.model.Song, alPulsar: () -> Unit) {
    Column(modifier = Modifier.width(140.dp).clickable { alPulsar() }) {
        AsyncImage(
            model = cancion.coverUrl,
            contentDescription = null,
            modifier = Modifier.size(140.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(cancion.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(cancion.artist, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
fun ItemGridAccesoRapido(
    titulo: String, 
    recursoImagen: Int? = null, 
    urlImagen: String? = null, 
    modifier: Modifier = Modifier, 
    alPulsar: () -> Unit
) {
    Surface(
        modifier = modifier.height(56.dp).clickable { alPulsar() },
        color = Color(0xFF333333),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (recursoImagen != null) {
                Image(painterResource(recursoImagen), null, Modifier.size(56.dp), contentScale = ContentScale.Crop)
            } else if (urlImagen != null) {
                AsyncImage(urlImagen, null, Modifier.size(56.dp), contentScale = ContentScale.Crop)
            }
            Text(titulo, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp), maxLines = 2)
        }
    }
}

@Composable
fun TarjetaMixHorizontal(nombre: String, urlImagen: String, alPulsar: () -> Unit) {
    Column(modifier = Modifier.width(150.dp).clickable { alPulsar() }) {
        AsyncImage(urlImagen, null, Modifier.size(150.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Mix de $nombre", color = Color.LightGray, fontSize = 12.sp)
    }
}

@Composable
fun ItemArtistaCircular(nombre: String, urlImagen: String, alPulsar: () -> Unit = {}) {
    Column(modifier = Modifier.width(120.dp).clickable { alPulsar() }, horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(urlImagen, null, Modifier.size(120.dp).clip(CircleShape), contentScale = ContentScale.Crop)
        Spacer(modifier = Modifier.height(8.dp))
        Text(nombre, color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun VistaPodcastsVacia() {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Podcasts, null, tint = Color.Gray, modifier = Modifier.size(60.dp))
        Spacer(Modifier.height(16.dp))
        Text("Todavía no sigues ningún podcast", color = Color.White, fontWeight = FontWeight.Bold)
        Text("Sigue tus programas favoritos para verlos aquí.", color = Color.Gray, fontSize = 13.sp)
    }
}
