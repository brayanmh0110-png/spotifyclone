# Documentación Detallada del Proyecto: Spotify Clone

Este proyecto es una aplicación de Android inspirada en Spotify, desarrollada con las últimas tecnologías y prácticas recomendadas en el ecosistema Android moderno.

## 1. Tecnologías Principales
- **Lenguaje:** Kotlin
- **Interfaz de Usuario:** Jetpack Compose (Declarativa)
- **Arquitectura:** MVVM (Model-View-ViewModel) + Repository Pattern
- **Base de Datos y Auth:** Firebase Firestore y Firebase Authentication
- **Multimedia:** Android MediaPlayer
- **Navegación:** Compose Navigation
- **Inyección de Dependencias/Estado:** ViewModel y StateFlow para reactividad.

---

## 2. Estructura del Proyecto
El proyecto sigue una organización limpia por capas:

### `com.example.spotifyclone`
- **`MainActivity.kt`**: Punto de entrada de la aplicación. Configura el tema, el controlador de navegación principal y gestiona la visibilidad del mini-reproductor global.
- **`model/`**: Contiene las clases de datos (POJOs/Data Classes) que representan las entidades del negocio.
  - `Song.kt`: Datos de la canción (URL, título, artista, duración, etc.).
  - `User.kt`: Información del usuario, incluyendo lista de favoritos.
  - `Album.kt`, `Artist.kt`, `Genre.kt`: Metadatos de la música.
  - `Playlist.kt`: Listas de reproducción.
  - `ActivityLog.kt`: Registro de acciones del usuario (ej. "Añadido a favoritos").
- **`repository/`**: Capa de abstracción de datos.
  - `MusicRepository.kt`: Centraliza todas las llamadas a Firebase Firestore. Maneja la obtención de canciones, álbumes, géneros, lógica de favoritos y registro de actividad.
- **`viewmodel/`**: Lógica de negocio y gestión de estado de la UI.
  - `AuthViewModel.kt`: Gestiona el inicio de sesión, registro y estado de la sesión con Firebase Auth.
  - `MusicViewModel.kt`: Controla el estado del reproductor (`MediaPlayer`), la carga de música y la interacción con el repositorio.
- **`navigation/`**: Gestión de rutas.
  - `NavGraph.kt`: Define las pantallas disponibles (`Screen`) y las transiciones animadas entre ellas.
- **`ui/`**: Componentes visuales.
  - `screens/`: Pantallas completas (Home, Login, Player, etc.).
  - `components/`: Componentes reutilizables (ej. `MiniPlayer`).
  - `theme/`: Configuración de colores, tipografía y formas de la marca (estética Spotify).

---

## 3. Funcionamiento Detallado

### A. Autenticación y Flujo de Usuario
1. **Bienvenida**: El usuario llega a `WelcomeScreen`.
2. **Registro/Login**: Se utiliza `AuthViewModel` para interactuar con Firebase. Se admiten flujos de email y opciones de login.
3. **Persistencia**: `MainActivity` observa el estado `isLoggedIn`. Si el usuario ya está autenticado, navega directamente a la `HomeScreen`.

### B. Gestión de Música (Firestore)
El `MusicRepository` utiliza **Corrutinas de Kotlin** y **Flow** para obtener datos en tiempo real o mediante peticiones únicas:
- Las colecciones en Firestore son: `users`, `songs`, `genres`, `playlists`, `activity_log`, `artists`, `albums`.
- Se implementan métodos como `getSongs()`, `getAlbums()`, y `toggleFavorite()` que actualizan arrays en Firestore de forma atómica usando `FieldValue.arrayUnion/Remove`.

### C. El Reproductor (MediaPlayer)
Gestionado en `MusicViewModel`:
- **Estado Global**: `currentSong`, `isPlaying`, `currentPosition` y `duration` son `StateFlow` que cualquier pantalla puede observar.
- **MiniPlayer**: Un componente anclado en la parte inferior de la mayoría de las pantallas (excepto login/player full) que permite control básico.
- **PlayerScreen**: Pantalla completa con controles avanzados, barra de progreso (SeekBar) y visualización de carátula.

### D. Navegación y Animaciones
- Se utiliza `AnimatedContentTransitionScope` para que las pantallas se deslicen de forma fluida (ej. el reproductor sube desde abajo).
- Las rutas están tipadas mediante una `sealed class Screen`, lo que evita errores de strings mágicos.

### E. Personalización
- **Favoritos**: Los usuarios pueden marcar canciones. Esto se guarda en su documento de usuario en Firestore y se refleja en tiempo real en la `LikedSongsScreen`.
- **Registro de Actividad**: Cada acción importante (como dar "Like") genera un log en la colección `activity_log` para auditoría o futuras recomendaciones.

---

## 4. Características Destacadas
- **Reactividad Completa**: La UI se actualiza automáticamente cuando los datos en el ViewModel cambian.
- **Diseño Moderno**: Uso exhaustivo de Material 3 y componentes personalizados para replicar la experiencia de Spotify.
- **Manejo de Errores**: Uso de `Result<T>` y bloques `try-catch` en operaciones asíncronas de red.
- **Escalabilidad**: La estructura permite añadir fácilmente nuevas funciones como búsqueda de canciones o descarga para modo offline.
