package com.example.spotifyclone.ui.screens

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
import com.example.spotifyclone.model.Song as MusicSong
import coil.compose.AsyncImage
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect

/**
 * LikedSongsScreen: Pantalla que muestra las canciones marcadas como "Favoritas".
 */
@Composable
fun LikedSongsScreen(
    controladorNavegacion: NavHostController,
    vistaModeloAutenticacion: AuthViewModel,
    vistaModeloMusica: MusicViewModel
) {
    // Obtenemos los estados necesarios
    val listaFavoritos by vistaModeloMusica.favorites.collectAsState()
    val estadoUsuario by vistaModeloAutenticacion.userState.collectAsState()

    // Cada vez que cambia el UID del usuario, recargamos sus favoritos
    LaunchedEffect(estadoUsuario.uid) {
        if (estadoUsuario.uid.isNotEmpty()) {
            vistaModeloMusica.loadFavorites(estadoUsuario.uid)
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { controladorNavegacion.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.Search, null, tint = Color.White)
                Spacer(Modifier.width(16.dp))
                Icon(Icons.Default.MoreVert, null, tint = Color.White)
            }
        }
    ) { rellenos ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(rellenos)) {
            // Cabecera morada estilo Spotify
            item {
                CabeceraFavoritos(listaFavoritos.size)
            }
            
            // Listado de canciones favoritas
            items(listaFavoritos) { cancion ->
                ItemCancionFavorita(cancion) {
                    vistaModeloMusica.playSong(cancion, listaFavoritos)
                }
            }
            
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun CabeceraFavoritos(cantidad: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = Brush.verticalGradient(colors = listOf(Color(0xFF503691), Color.Black)))
            .padding(16.dp)
    ) {
        Text("Tus me gustas", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("$cantidad canciones", color = Color.LightGray, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))
        // Botón Play flotante a la derecha
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.align(Alignment.CenterEnd).size(56.dp).clip(CircleShape).background(Color(0xFF1DB954)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun ItemCancionFavorita(cancion: MusicSong, alPulsar: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { alPulsar() }.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (cancion.coverUrl.isNotEmpty()) {
            AsyncImage(cancion.coverUrl, null, Modifier.size(50.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
        } else {
            // Si no hay portada, usamos la imagen de "corazon" que tienes en recursos
            Image(painterResource(R.drawable.corazon), null, Modifier.size(50.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(cancion.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(cancion.artist, color = Color.Gray, fontSize = 14.sp)
        }
        Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
    }
}
