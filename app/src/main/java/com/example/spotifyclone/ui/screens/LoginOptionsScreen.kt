package com.example.spotifyclone.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.spotifyclone.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginOptionsScreen(navController: NavHostController) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Logo placeholder
            Text("🎧", fontSize = 50.sp)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Iniciar sesión en Spotify",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            Column {
                LoginOptionButton(
                    text = "Continuar con tu email",
                    containerColor = Color(0xFF1DB954),
                    contentColor = Color.Black,
                    icon = Icons.Default.Email,
                    onClick = { navController.navigate(Screen.LoginEmail.route) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                LoginOptionButton(
                    text = "Continuar con número de teléfono",
                    border = BorderStroke(1.dp, Color.White),
                    onClick = { Toast.makeText(context, "Inicio por teléfono próximamente", Toast.LENGTH_SHORT).show() }
                )
                Spacer(modifier = Modifier.height(12.dp))
                LoginOptionButton(
                    text = "Continuar con Google",
                    border = BorderStroke(1.dp, Color.White),
                    onClick = { Toast.makeText(context, "Inicio con Google próximamente", Toast.LENGTH_SHORT).show() }
                )
                Spacer(modifier = Modifier.height(12.dp))
                LoginOptionButton(
                    text = "Iniciar sesión con Facebook",
                    border = BorderStroke(1.dp, Color.White),
                    onClick = { Toast.makeText(context, "Inicio con Facebook próximamente", Toast.LENGTH_SHORT).show() }
                )
                Spacer(modifier = Modifier.height(12.dp))
                LoginOptionButton(
                    text = "Continuar con Apple",
                    border = BorderStroke(1.dp, Color.White),
                    onClick = { Toast.makeText(context, "Inicio con Apple próximamente", Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }
}

@Composable
fun LoginOptionButton(
    text: String,
    containerColor: Color = Color.Transparent,
    contentColor: Color = Color.White,
    icon: ImageVector? = null,
    border: BorderStroke? = null,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        shape = CircleShape,
        border = border
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(36.dp)) // To center text better when icon exists
            } else {
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
