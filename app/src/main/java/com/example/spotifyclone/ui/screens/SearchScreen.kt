package com.example.spotifyclone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.spotifyclone.viewmodel.MusicViewModel
import com.example.spotifyclone.model.Song

/**
 * SearchScreen: Pantalla de búsqueda y descubrimiento.
 * Permite buscar canciones reales y explorar categorías predefinidas.
 */
@Composable
fun SearchScreen(
    controladorNavegacion: NavHostController,
    vistaModeloMusica: MusicViewModel
) {
    // Estados reactivos para la búsqueda
    var textoBusqueda by remember { mutableStateOf("") }
    val resultadosBusqueda by vistaModeloMusica.searchResults.collectAsState()
    val estaBuscando by vistaModeloMusica.isSearching.collectAsState()

    Scaffold(
        containerColor = Color.Black
    ) { rellenos ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(rellenos)
                .padding(horizontal = 16.dp)
        ) {
            // Cabecera: Botón de volver y Título
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                IconButton(onClick = { controladorNavegacion.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                }
                Text("Buscar", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            // --- COMPONENTE: BARRA DE BÚSQUEDA ---
            TextField(
                value = textoBusqueda,
                onValueChange = { 
                    textoBusqueda = it
                    vistaModeloMusica.searchSongs(it) // Dispara la búsqueda en la API
                },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                placeholder = { Text("¿Qué quieres escuchar?", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Black) },
                trailingIcon = {
                    if (textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { 
                            textoBusqueda = "" 
                            vistaModeloMusica.searchSongs("") // Limpia resultados
                        }) {
                            Icon(Icons.Default.Close, null, tint = Color.Black)
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lógica de visualización de resultados
            if (estaBuscando) {
                // Indicador de carga mientras la API responde
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1DB954))
                }
            } else if (textoBusqueda.isNotEmpty()) {
                // Lista de canciones encontradas
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(resultadosBusqueda) { cancion ->
                        ItemCancionBusqueda(cancion) {
                            vistaModeloMusica.playSong(cancion, resultadosBusqueda)
                        }
                    }
                }
            } else {
                // SECCIÓN: Exploración (cuando no hay búsqueda activa)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    item {
                        Text("Descubre algo nuevo", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            val sugerencias = listOf(
                                Pair("pop", "https://picsum.photos/seed/10/300/500"),
                                Pair("rock", "https://picsum.photos/seed/11/300/500"),
                                Pair("lofi", "https://picsum.photos/seed/12/300/500")
                            )
                            items(sugerencias) { (termino, url) ->
                                TarjetaSugerenciaVertical(
                                    titulo = "#$termino",
                                    urlImagen = url,
                                    alPulsar = {
                                        textoBusqueda = termino
                                        vistaModeloMusica.searchSongs(termino)
                                    }
                                )
                            }
                        }
                    }

                    item {
                        Text("Explorar todo", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        CuadriculaExplorar(
                            alBuscar = { termino ->
                                textoBusqueda = termino
                                vistaModeloMusica.searchSongs(termino)
                            }
                        )
                    }
                }
            }
        }
    }
}

// --- SUB-COMPONENTES ---

@Composable
fun ItemCancionBusqueda(cancion: Song, alPulsar: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { alPulsar() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = cancion.coverUrl,
            contentDescription = null,
            modifier = Modifier.size(50.dp).clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(cancion.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Text(cancion.artist, color = Color.Gray, fontSize = 14.sp, maxLines = 1)
        }
    }
}

@Composable
fun TarjetaSugerenciaVertical(titulo: String, urlImagen: String, alPulsar: () -> Unit = {}) {
    Box(modifier = Modifier.width(140.dp).height(200.dp).clip(RoundedCornerShape(8.dp)).clickable { alPulsar() }) {
        AsyncImage(urlImagen, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Text(
            text = titulo,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
        )
    }
}

@Composable
fun CuadriculaExplorar(alBuscar: (String) -> Unit = {}) {
    val categorias = listOf(
        Pair("Música", Color(0xFFE13300)),
        Pair("Podcasts", Color(0xFF1E3264)),
        Pair("En vivo", Color(0xFF8D67AB)),
        Pair("Para ti", Color(0xFF1E3264))
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        categorias.chunked(2).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                fila.forEach { (nombre, color) ->
                    TarjetaCategoria(
                        nombre = nombre,
                        color = color,
                        modifier = Modifier.weight(1f),
                        alPulsar = { alBuscar(nombre) }
                    )
                }
            }
        }
    }
}

@Composable
fun TarjetaCategoria(nombre: String, color: Color, modifier: Modifier = Modifier, alPulsar: () -> Unit = {}) {
    Box(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .clickable { alPulsar() }
            .padding(8.dp)
    ) {
        Text(nombre, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
