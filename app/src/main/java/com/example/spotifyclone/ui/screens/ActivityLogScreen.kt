package com.example.spotifyclone.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.spotifyclone.viewmodel.AuthViewModel
import com.example.spotifyclone.viewmodel.MusicViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * ActivityLogScreen: Muestra el historial de acciones del usuario en la app.
 * Lista las últimas reproducciones, creaciones de playlists, etc.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogScreen(
    navController: NavHostController,
    musicViewModel: MusicViewModel,
    authViewModel: AuthViewModel
) {
    val logs by musicViewModel.activityLogs.collectAsState()
    val userState by authViewModel.userState.collectAsState()

    // Cargamos el historial cada vez que se abre la pantalla
    LaunchedEffect(userState.uid) {
        if (userState.uid.isNotEmpty()) {
            musicViewModel.cargarHistorial(userState.uid)
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Historial de actividad", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        if (logs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay actividad reciente", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                items(logs) { log ->
                    LogItem(log)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
                }
            }
        }
    }
}

/**
 * LogItem: Una fila del historial con icono y fecha formateada.
 */
@Composable
fun LogItem(log: com.example.spotifyclone.model.ActivityLog) {
    // Formateamos el Timestamp de Firebase a algo legible (Ej: 15 May, 14:30)
    val dateStr = remember(log.timestamp) {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        sdf.format(log.timestamp.toDate())
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.History, null, tint = Color(0xFF1DB954), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(log.action, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(dateStr, color = Color.Gray, fontSize = 12.sp)
        }
    }
}
