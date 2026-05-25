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
  - `LibraryScreen`: Lista completa de las 50 canciones obtenidas.
  - `PlayerScreen`: Reproductor a pantalla completa con controles.

---

## 3. Funciones Avanzadas Implementadas

### A. Sembrado Automático y Paralelo (Seed Data)
El proyecto cuenta con un sistema inteligente de carga de datos en `MusicRepository.kt`:
1. **Limpieza**: Al detectar cambios o inicialización, limpia las colecciones de Firestore para evitar datos duplicados o "basura" de pruebas anteriores.
2. **Búsqueda en Paralelo**: Utiliza corrutinas para buscar 50 canciones simultáneamente mediante peticiones HTTP. Esto reduce el tiempo de carga de minutos a segundos.
3. **Persistencia**: La información obtenida (portadas HD, audios, nombres) se guarda en Firestore, cumpliendo la regla de que la App solo consume datos de la base de datos propia.

### B. Integración con iTunes API
Se utiliza como fuente de datos real para obtener:
- **Audio Previo**: Archivos MP3 directos (30 segundos).
- **Metadatos**: Títulos de canciones y nombres de artistas reales.
- **Arte de Álbum**: URLs de imágenes originales en alta resolución (600x600).

### C. Sistema de Biblioteca Dinámica
La pantalla de Biblioteca consulta la colección `songs` en tiempo real, mostrando el conteo total y permitiendo la reproducción inmediata de cualquier tema.

---

## 4. Funcionamiento del Reproductor
- El `MediaPlayer` de Android recibe la `previewUrl` obtenida de la API.
- Se gestiona un estado global (`currentSong`, `isPlaying`) para que la música no se detenga al cambiar de pantalla.
- **Limitación Técnica**: Los audios duran 30 segundos debido a que son "previews" gratuitos proporcionados por Apple para desarrolladores. Una integración de canciones completas requeriría APIs de pago o SDKs complejos de terceros.
