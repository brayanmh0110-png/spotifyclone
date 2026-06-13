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
 */
@Composable
fun SearchScreen(
    navController: NavHostController,
    musicViewModel: MusicViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by musicViewModel.searchResults.collectAsState()
    val isSearching by musicViewModel.isSearching.collectAsState()

    Scaffold(
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                }
                Text("Buscar", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            TextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    musicViewModel.searchSongs(it)
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

            if (isSearching) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1DB954))
                }
            } else if (searchQuery.isNotEmpty()) {
                if (searchResults.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("No se encontraron resultados", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Asegúrate de que todo esté bien escrito.", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(searchResults) { song ->
                            SearchSongItem(song) {
                                musicViewModel.playSong(song, searchResults)
                            }
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    item {
                        Text("Descubre algo nuevo", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            val suggestions = listOf(
                                Pair("pop", "https://picsum.photos/seed/10/300/500"),
                                Pair("rock", "https://picsum.photos/seed/11/300/500"),
                                Pair("lofi", "https://picsum.photos/seed/12/300/500")
                            )
                            items(suggestions) { (term, url) ->
                                DiscoveryCard(
                                    title = "#$term",
                                    imageUrl = url,
                                    onClick = {
                                        searchQuery = term
                                        musicViewModel.searchSongs(term)
                                    }
                                )
                            }
                        }
                    }

                    item {
                        Text("Explorar todo", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        ExploreGrid(
                            onSearch = { term ->
                                searchQuery = term
                                musicViewModel.searchSongs(term)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchSongItem(song: Song, onClick: () -> Unit) {
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
        Column {
            Text(song.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Text(song.artist, color = Color.Gray, fontSize = 14.sp, maxLines = 1)
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
