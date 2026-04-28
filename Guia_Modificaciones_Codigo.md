# 🛠️ Guía de Modificaciones Rápidas (Para el examen/clase)

Si el profesor te pide cambiar algo en vivo, aquí tienes los "machetes" (cheat sheets) más comunes para que sepas dónde tocar:

## 1. Cambiar Colores o Estilos
*   **¿Dónde?**: `ui/theme/Color.kt` o directamente en el Composable usando `Color.Red`, `Color(0xFF...)`.
*   **Fondo de pantalla**: Busca el `Scaffold` o `Column` principal y cambia `containerColor = Color.Black` por el color que te pida.

## 2. Agregar un campo nuevo (Ej: Nombre de usuario)
1.  **Modelo**: Ve a `model/User.kt` y añade `val name: String = ""`.
2.  **ViewModel**: En `AuthViewModel.kt`, añade una función `onNameChange(name: String)` similar a las de email/password.
3.  **UI**: En `RegisterScreen.kt`, copia un `TextField`, cámbiale la etiqueta y conéctalo a la nueva función del ViewModel.

## 3. Modificar Listas (Canciones o Artistas)
*   **Añadir más elementos**: En `LikedSongsScreen.kt` o `HomeScreen.kt`, busca la lista (ej. `likedSongs`) y simplemente añade más objetos al `listOf(...)` o cambia el rango `(1..20)` por `(1..50)`.
*   **Cambiar diseño de un item**: Busca el componente que empieza con "Item" (ej: `SongItem` o `ArtistItem`) y cambia el `Modifier.size`, `fontSize` o añade un `Icon`.

## 4. Cambiar la Navegación
*   **Cambiar pantalla de inicio**: En `NavGraph.kt`, cambia `startDestination = Screen.Welcome.route` por la pantalla que quieras que salga primero.
*   **Añadir o cambiar una acción de clic**: 
    *   Busca el componente que tiene el clic (ej: en `HomeScreen.kt`).
    *   Verás algo como: `onClick = { navController.navigate(Screen.LikedSongs.route) }`.
    *   Si te pide que vaya a otra pantalla (ej: AlbumDetail), simplemente cámbialo por: `navController.navigate(Screen.AlbumDetail.route)`.
    *   **Importante**: El nombre de la ruta debe existir en `NavGraph.kt` dentro de la `sealed class Screen`.

## 5. Modificadores Comunes (Los que más se usan)
Si te pide "mueve esto", "dale espacio", usa estos dentro de `Modifier`:
*   `.padding(16.dp)` -> Espacio interno.
*   `.fillMaxWidth()` -> Ocupar todo el ancho.
*   `.size(100.dp)` -> Tamaño fijo (ancho y alto).
*   `.clip(CircleShape)` -> Hacer algo redondo.
*   `.background(Color.Gray)` -> Color de fondo de ese elemento.

## 6. ¿Cómo encontrar cosas rápido?
*   **Presiona `Ctrl + Shift + F`**: Para buscar cualquier texto en todo el proyecto.
*   **Presiona `Shift` dos veces**: Para buscar un archivo por su nombre (ej: escribes "Home" y te sale `HomeScreen.kt`).
*   **Ctrl + Clic en una función**: Te lleva directamente a donde está definida.

## 7. Si algo falla (Logcat)
Si la app se cierra, dile: *"Voy a revisar el Logcat para ver el error"* (Eso suena muy profesional). Busca letras rojas, ahí te dirá la línea exacta del fallo.

## 8. Otros ejemplos de lo que te podría pedir el profesor

### A. "Haz que el botón de Login solo se active si el password tiene más de 6 caracteres"
*   **¿Dónde?**: En `LoginEmailScreen.kt`.
*   **¿Cómo?**: Busca el `Button` y añade el parámetro `enabled`:
    ```kotlin
    Button(
        onClick = { ... },
        enabled = userState.password.length > 6 // Solo se activa si es mayor a 6
    ) { ... }
    ```

### B. "Cambia el orden de los elementos en la Home"
*   **¿Dónde?**: En `HomeScreen.kt` dentro del `LazyColumn`.
*   **¿Cómo?**: Simplemente selecciona un bloque `item { ... }` y muévelo arriba o abajo de otro bloque `item`. Es como cortar y pegar bloques de Lego.

### C. "Haz que las fotos de los artistas sean cuadradas en lugar de redondas"
*   **¿Dónde?**: En `HomeScreen.kt`, busca la función `ArtistItem`.
*   **¿Cómo?**: Busca la línea `.clip(CircleShape)` y cámbiala por `.clip(RoundedCornerShape(8.dp))` o quítale el clip para que sea un cuadrado perfecto.

### D. "Cambia el icono de la barra inferior (BottomBar)"
*   **¿Dónde?**: En `HomeScreen.kt`, busca la función `HomeBottomBar`.
*   **¿Cómo?**: En la lista `items`, cambia `Icons.Default.Home` por otro como `Icons.Default.Favorite` o `Icons.Default.Person`.

### E. "Añade un mensaje (Toast) cuando se haga clic en una canción"
*   **¿Dónde?**: En `LikedSongsScreen.kt`.
*   **¿Cómo?**: 
    1. Necesitas el context: `val context = LocalContext.current`.
    2. En el `Modifier.clickable { ... }` añade:
    ```kotlin
    Toast.makeText(context, "Reproduciendo: ${song.title}", Toast.LENGTH_SHORT).show()
    ```

### F. "Cambia el tamaño de la letra de los títulos"
*   **¿Dónde?**: En cualquier pantalla, busca el `Text` y cambia el parámetro `fontSize = 22.sp`. Recuerda que en Android:
    *   **sp**: Para textos (se escala con la configuración del usuario).
    *   **dp**: Para tamaños de botones, márgenes y espacios.

### G. "Cambia el color de la barra de estado (donde sale la hora/batería)"
*   **¿Dónde?**: Esto suele estar en `MainActivity.kt` o en el archivo del Tema (`ui/theme/Theme.kt`).
*   **Explicación**: Dile que se gestiona mediante `SystemUiController` o directamente en el tema de Material3 para que combine con el diseño oscuro de la app.

---
**Recuerda:** Siempre que hagas un cambio, guarda (`Ctrl+S`) y si es un cambio grande, dale al botón de "Run" (el rayito o el play verde) para que se vea en el emulador.
