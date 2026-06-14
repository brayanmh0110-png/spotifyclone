package com.example.spotifyclone.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.spotifyclone.viewmodel.MusicViewModel
import com.example.spotifyclone.viewmodel.AuthViewModel
import com.example.spotifyclone.model.Song

/**
 * SearchScreen: Pantalla de búsqueda y descubrimiento.
 * Permite buscar cualquier canción en iTunes y guardarla como favorita.
 */
@Composable
fun SearchScreen(
    navController: NavHostController,
    musicViewModel: MusicViewModel,
    authViewModel: AuthViewModel // Necesitamos el authViewModel para el UID del usuario
) {
    // --- ESTADOS LOCALES ---
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by musicViewModel.searchResults.collectAsState()
    val isSearching by musicViewModel.isSearching.collectAsState()
    val userState by authViewModel.userState.collectAsState()
    val favorites by musicViewModel.favorites.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Título de la sección
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                }
                Text("Buscar", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            // --- CAMPO DE TEXTO (Barra de búsqueda) ---
            TextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    musicViewModel.searchSongs(it) // Dispara la búsqueda en el ViewModel
                },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                placeholder = { Text("¿Qué quieres escuchar?", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Black) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { 
                            searchQuery = "" 
                            musicViewModel.searchSongs("")
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

            // --- CONTENIDO VARIABLE ---
            // Usamos Crossfade para que el cambio entre "Sugerencias" y "Resultados" sea suave.
            Crossfade(targetState = isSearching || searchQuery.isNotEmpty(), label = "SearchState") { hasQuery ->
                if (isSearching) {
                    // Estado: Buscando (Spinner)
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF1DB954))
                    }
                } else if (hasQuery) {
                    // Estado: Mostrando resultados de iTunes
                    if (searchResults.isEmpty()) {
                        EmptySearchResults()
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(searchResults) { song ->
                                // Verificamos si la canción ya es favorita para pintar el corazón
                                val isFavorite = favorites.any { it.id == song.id }
                                SearchSongItem(
                                    song = song,
                                    isFavorite = isFavorite,
                                    onFavoriteClick = {
                                        musicViewModel.toggleFavorite(userState.uid, song)
                                        Toast.makeText(context, if (isFavorite) "Eliminado de favoritos" else "Agregado a favoritos", Toast.LENGTH_SHORT).show()
                                    },
                                    onClick = {
                                        musicViewModel.playSong(song, searchResults)
                                        musicViewModel.registrarReproduccion(userState.uid, song)
                                    }
                                )
                            }
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                } else {
                    // Estado: Pantalla inicial de descubrimiento
                    DiscoverySection(
                        onSuggestionClick = { term ->
                            searchQuery = term
                            musicViewModel.searchSongs(term)
                        }
                    )
                }
            }
        }
    }
}

/**
 * SearchSongItem: Fila individual para cada canción encontrada.
 * Incluye título, artista, portada y el botón de corazón.
 */
@Composable
fun SearchSongItem(
    song: Song, 
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.coverUrl,
            contentDescription = null,
            modifier = Modifier.size(50.dp).clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(song.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Text(song.artist, color = Color.Gray, fontSize = 14.sp, maxLines = 1)
        }
        // Botón Corazón (Favoritos)
        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = if (isFavorite) Color(0xFF1DB954) else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EmptySearchResults() {
    Box(Modifier.fillMaxSize().padding(top = 100.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("No se encontraron resultados", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Asegúrate de que todo esté bien escrito.", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun DiscoverySection(onSuggestionClick: (String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item {
            Text("Descubre algo nuevo", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val suggestions = listOf(
                    Pair("pop", "https://images.unsplash.com/photo-1514525253361-bee8718a74a2?w=500"),
                    Pair("rock", "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=500"),
                    Pair("lofi", "https://images.unsplash.com/photo-1516280440614-37939bbacd81?w=500")
                )
                items(suggestions) { (term, url) ->
                    DiscoveryCard(
                        title = "#$term",
                        imageUrl = url,
                        onClick = { onSuggestionClick(term) }
                    )
                }
            }
        }

        item {
            Text("Explorar todo", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            ExploreGrid(onSearch = onSuggestionClick)
        }
    }
}

@Composable
fun DiscoveryCard(title: String, imageUrl: String, onClick: () -> Unit = {}) {
    Box(modifier = Modifier.width(140.dp).height(200.dp).clip(RoundedCornerShape(8.dp)).clickable { onClick() }) {
        AsyncImage(imageUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
        )
    }
}

@Composable
fun ExploreGrid(onSearch: (String) -> Unit = {}) {
    val categories = listOf(
        Pair("Música", Color(0xFFE13300)),
        Pair("Podcasts", Color(0xFF1E3264)),
        Pair("En vivo", Color(0xFF8D67AB)),
        Pair("Para ti", Color(0xFF1E3264))
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        categories.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (name, color) ->
                    CategoryCard(
                        name = name,
                        color = color,
                        modifier = Modifier.weight(1f),
                        onClick = { onSearch(name) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryCard(name: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Box(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Text(name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
