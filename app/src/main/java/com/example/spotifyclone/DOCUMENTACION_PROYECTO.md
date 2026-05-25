# Documentación Detallada del Proyecto: Spotify Clone

Este proyecto es una aplicación de Android inspirada en Spotify, desarrollada con Jetpack Compose y Firebase, que consume datos reales de música mediante la API de iTunes.

## 1. Tecnologías Principales
- **Lenguaje:** Kotlin 2.2.10
- **Interfaz de Usuario:** Jetpack Compose (Material 3)
- **Arquitectura:** MVVM (Model-View-ViewModel) + Repository Pattern
- **Base de Datos y Auth:** Firebase Firestore y Firebase Authentication
- **Multimedia:** Android MediaPlayer (Streaming de URLs externas)
- **Navegación:** Compose Navigation con transiciones animadas
- **Consumo de Datos:** iTunes Search API (REST)
- **Concurrencia:** Corrutinas de Kotlin (Parallel Seeding con `async`/`awaitAll`)

---

## 2. Estructura del Proyecto

### Capas Principales
- **`model/`**: Clases de datos (`Song`, `Album`, `Artist`, `Genre`).
- **`repository/`**: `MusicRepository.kt` gestiona la lógica de Firestore y la integración con la API de iTunes.
- **`viewmodel/`**: 
  - `MusicViewModel.kt`: Controla el estado del reproductor y la sincronización de datos.
  - `AuthViewModel.kt`: Gestiona la sesión del usuario.
- **`ui/screens/`**: 
  - `HomeScreen`: Dashboard principal con mixes y artistas.
  - `SearchScreen`: Interfaz de búsqueda que consulta la API de iTunes en tiempo real.
  - `LibraryScreen`: Lista completa de las 50 canciones obtenidas.
  - `PlayerScreen`: Reproductor a pantalla completa con controles.

---

## 3. Funciones Avanzadas Implementadas

### A. Sembrado Automático y Paralelo (Seed Data)
... (anteriormente documentado)

### B. Sistema de Búsqueda Global (iTunes API)
Se ha implementado una nueva capacidad de búsqueda:
1. **Consulta en Tiempo Real**: El usuario puede escribir cualquier término y la App realizará una petición a la API de iTunes.
2. **Resultados Dinámicos**: Muestra hasta 10 resultados con portadas, nombres y audio real, permitiendo su reproducción instantánea.
3. **Optimización**: Gestión de estados de carga (`isSearching`) y limpieza de resultados.

### C. Integración con iTunes API
... (anteriormente documentado)

### C. Sistema de Biblioteca Dinámica
La pantalla de Biblioteca consulta la colección `songs` en tiempo real, mostrando el conteo total y permitiendo la reproducción inmediata de cualquier tema.

---

## 4. Funcionamiento del Reproductor
- El `MediaPlayer` de Android recibe la `previewUrl` obtenida de la API.
- Se gestiona un estado global (`currentSong`, `isPlaying`) para que la música no se detenga al cambiar de pantalla.
- **Limitación Técnica**: Los audios duran 30 segundos debido a que son "previews" gratuitos proporcionados por Apple para desarrolladores. Una integración de canciones completas requeriría APIs de pago o SDKs complejos de terceros.
