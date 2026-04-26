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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.spotifyclone.navigation.Screen
import com.example.spotifyclone.R


@Composable
fun HomeScreen(navController: NavHostController) {
    var selectedFilter by remember { mutableStateOf("Todas") }

    Scaffold(
        containerColor = Color.Black,
        topBar = { HomeTopBar(selectedFilter) { selectedFilter = it } },
        bottomBar = { HomeBottomBar() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            // Grid de Tarjetas (2x2)
            item {
                Column {
                    Row(Modifier.fillMaxWidth()) {
                        HomeGridItem("Tus me gustas", R.drawable.corazon, Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        HomeGridItem(
                            "Dios de Generaciones",
                            R.drawable.album_generaciones, //agregamos la imagen de album_generaciones
                            Modifier.weight(1f),
                            onClick = { navController.navigate(Screen.AlbumDetail.route) }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth()) {
                        HomeGridItem("Miel San Marcos", R.drawable.mielsan, Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        HomeGridItem("Radio Miel San Marcos", R.drawable.radiomiel, Modifier.weight(1f))
                    }
                }
            }

            item {
                SectionTitle("Tus mixes")

                // Creamos una lista de objetos anónimos (o Pares)
                // que asocian el nombre con el recurso drawable
                val myMixes = listOf(
                    "Redimi2" to R.drawable.redimimix,
                    "Barak" to R.drawable.barakmix, // Usa mielsan mientras consigues las otras fotos
                    "Marco Barrientos" to R.drawable.marcosmix,
                    "Miel San Marcos" to R.drawable.mielmix
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(myMixes) { mix ->
                        // mix.first es el nombre (String)
                        // mix.second es la imagen (Int)
                        MixItem(name = mix.first, imageRes = mix.second)
                    }
                }
            }
            item {
                SectionTitle("Tus artistas favoritos")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val artists = listOf(
                        Pair("Miel San Marcos", R.drawable.mielsan),
                            Pair("Waleska Morales", R.drawable.waleska),
                                Pair("Averly Morillo", R.drawable.averly),
                                    Pair("Redimi2", R.drawable.predimi2),
                                        Pair("Barak", R.drawable.pbarak),
                                            Pair("Marco Barriento", R.drawable.pmarcosbarri)
                    )
                    items(artists) { artist ->
                        ArtistItem(artist.first, artist.second)
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun HomeTopBar(selectedFilter: String, onFilterClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(35.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip("Todas", selectedFilter == "Todas") { onFilterClick("Todas") }
                FilterChip("Música", selectedFilter == "Música") { onFilterClick("Música") }
                FilterChip("Podcasts", selectedFilter == "Podcasts") { onFilterClick("Podcasts") }
                if (selectedFilter == "Música") {
                    FilterChip("Siguiendo", false) {}
                }
            }
        }
    }
}

@Composable
fun FilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = CircleShape,
        color = if (isSelected) Color(0xFF1DB954) else Color(0xFF333333)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HomeGridItem(title: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit = {}) {
    HomeGridItemContent(title, modifier, onClick) {
        Icon(icon, contentDescription = null, tint = Color.White)
    }
}

@Composable
fun HomeGridItem(title: String, imageRes: Int, modifier: Modifier, onClick: () -> Unit = {}) {
    HomeGridItemContent(title, modifier, onClick) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun HomeGridItemContent(
    title: String,
    modifier: Modifier,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .clickable { onClick() },
        color = Color(0xFF333333),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(56.dp)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp),
                maxLines = 2
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
fun MixItem(name: String, imageRes: Int) {
    Column(modifier = Modifier.width(150.dp)) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(8.dp)), // El estilo redondeado de Spotify
            contentScale = ContentScale.Crop // Para que la foto no se deforme
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Mix de $name",
            color = Color.LightGray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun ArtistItem(name: String, imageRes: Int) {
    Column(
        modifier = Modifier.width(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = name, color = Color.White, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun HomeBottomBar() {
    NavigationBar(containerColor = Color.Black.copy(alpha = 0.9f)) {
        val items = listOf(
            Triple("Inicio", Icons.Default.Home, true),
            Triple("Buscar", Icons.Default.Search, false),
            Triple("Biblioteca", Icons.Default.LibraryMusic, false),
            Triple("Premium", Icons.Default.WorkspacePremium, false),
            Triple("Crear", Icons.Default.AddCircle, false)
        )
        items.forEach { (label, icon, isSelected) ->
            NavigationBarItem(
                selected = isSelected,
                onClick = {},
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = Color.White,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
