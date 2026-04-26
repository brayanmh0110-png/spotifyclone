package com.example.spotifyclone.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.spotifyclone.navigation.Screen
import com.example.spotifyclone.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val userState by authViewModel.userState.collectAsState()
    var showEmailFields by remember { mutableStateOf(false) }

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
                text = "Regístrate para empezar a escuchar contenido",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            if (!showEmailFields) {
                SocialRegisterButtons(onEmailClick = { showEmailFields = true })
            } else {
                EmailRegistrationForm(
                    email = userState.email,
                    password = userState.password,
                    onEmailChange = { authViewModel.onEmailChange(it) },
                    onPasswordChange = { authViewModel.onPasswordChange(it) },
                    onRegisterClick = {
                        if (authViewModel.register()) {
                            navController.navigate(Screen.Home.route)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "¿Ya tienes una cuenta?",
                color = Color.White,
                fontSize = 14.sp
            )
            TextButton(onClick = { navController.navigate(Screen.LoginOptions.route) }) {
                Text(
                    text = "Iniciar sesión",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SocialRegisterButtons(onEmailClick: () -> Unit) {
    Column {
        RegisterButton(
            text = "Continuar con tu email",
            containerColor = Color(0xFF1DB954),
            contentColor = Color.Black,
            onClick = onEmailClick
        )
        Spacer(modifier = Modifier.height(12.dp))
        RegisterButton(text = "Continuar con número de teléfono", border = BorderStroke(1.dp, Color.White))
        Spacer(modifier = Modifier.height(12.dp))
        RegisterButton(text = "Continuar con Google", border = BorderStroke(1.dp, Color.White))
        Spacer(modifier = Modifier.height(12.dp))
        RegisterButton(text = "Continuar con Apple", border = BorderStroke(1.dp, Color.White))
    }
}

@Composable
fun EmailRegistrationForm(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Contraseña", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954)),
            shape = CircleShape
        ) {
            Text("Registrarse", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RegisterButton(
    text: String,
    containerColor: Color = Color.Transparent,
    contentColor: Color = Color.White,
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
        Text(text = text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
