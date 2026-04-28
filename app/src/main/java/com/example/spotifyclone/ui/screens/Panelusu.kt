package com.example.spotifyclone.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.spotifyclone.R

@Composable
fun PanelUsuScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // 1 y 2. Perfil y Nombre
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.perfil),
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Grupo Dev",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Ver perfil",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        // 3. Línea blanca horizontal delgada
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = 0.5.dp,
            color = Color.White.copy(alpha = 0.3f)
        )

        // 4. Opciones de lista
        OptionItem(Icons.Default.AddCircle, "Agregar Cuenta")
        OptionItem(Icons.Default.History, "Recientes")
        OptionItem(Icons.Default.Campaign, "Tus avisos")
        OptionItem(Icons.Default.Settings, "Configuración y privacidad")

        Spacer(modifier = Modifier.height(24.dp))

        // Sección de Activity e Invitar
        Row(modifier = Modifier.fillMaxWidth()) {
            // Activity
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.perfil),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp).clip(CircleShape)
                )
                Text("Activity", color = Color.White, fontSize = 12.sp)
                Text("Activar", color = Color.Gray, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Invitar amigos
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                }
                Text("Invitar a amigos", color = Color.White, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sección Mensajes
        Text(
            text = "Mensajes",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Comparte lo que te gusta con tus personas favoritas en Spotify",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.Description, contentDescription = null, tint = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nuevo Mensaje
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.NoteAdd, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Nuevo mensaje", color = Color.White, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun OptionItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, color = Color.White, fontSize = 16.sp)
    }
}