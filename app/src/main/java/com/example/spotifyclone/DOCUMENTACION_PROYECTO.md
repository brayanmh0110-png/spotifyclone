# Documentación Detallada del Proyecto: Spotify Clone

Este proyecto es una aplicación de Android inspirada en Spotify, desarrollada con Jetpack Compose y Firebase, que consume datos reales de música mediante la API de iTunes para ofrecer una experiencia dinámica y profesional.

## 1. Tecnologías Principales
- **Lenguaje:** Kotlin 2.2.10
- **Interfaz de Usuario:** Jetpack Compose (Material 3) - Diseño totalmente declarativo.
- **Arquitectura:** MVVM (Model-View-ViewModel) + Repository Pattern para una clara separación de responsabilidades.
- **Base de Datos y Auth:** Firebase Firestore y Firebase Authentication.
- **Multimedia:** Android MediaPlayer (Streaming de URLs externas con gestión de estados).
- **Navegación:** Compose Navigation con rutas tipadas y transiciones animadas de entrada y salida.
- **Consumo de Datos:** iTunes Search API (Peticiones REST a Apple Music).
- **Concurrencia:** Corrutinas de Kotlin y Dispatchers especializados (Uso de `async` y `awaitAll` para procesos en paralelo).

---

## 2. Estructura del Proyecto

### Capas Principales
- **`model/`**: Definición de entidades (`Song`, `Album`, `Artist`, `Genre`). Estructura preparada para la deserialización automática de Firestore.
- **`repository/`**: `MusicRepository.kt` centraliza la lógica de datos. Realiza la limpieza de base de datos, las búsquedas en la API de iTunes y la persistencia en las colecciones de Firestore.
- **`viewmodel/`**: 
  - `MusicViewModel.kt`: Corazón de la app. Gestiona el estado global de la música, el reproductor y los resultados de búsqueda.
  - `AuthViewModel.kt`: Controla el flujo de registro, inicio y cierre de sesión.
- **`ui/screens/`**: 
  - `HomeScreen`: Dashboard con secciones dinámicas (Mixes, Recomendados, Artistas).
  - `SearchScreen`: Buscador global reactivo conectado a la API de iTunes.
  - `LibraryScreen`: Vista de la colección completa de 50 canciones obtenidas por el sembrado de datos.
  - `PlayerScreen`: Interfaz detallada con controles de reproducción avanzados.

---

## 3. Funciones Avanzadas Implementadas

### A. Sembrado de Datos Inteligente (Parallel Seeding)
El sistema rellena automáticamente la base de datos de Firestore al abrir la App:
1. **Limpieza Profunda**: Elimina documentos antiguos de todas las colecciones para evitar rastro de datos de prueba.
2. **Peticiones en Paralelo**: Ejecuta 50 búsquedas simultáneas en la API de iTunes, reduciendo el tiempo de carga de minutos a solo unos segundos.
3. **Persistencia Total**: Guarda canciones, artistas y álbumes vinculados por IDs en Firestore para cumplir con la regla de consumo de base de datos propia.

### B. Sistema de Búsqueda Global
Permite al usuario descubrir música fuera del catálogo inicial:
- **Consulta en Tiempo Real**: Realiza peticiones asíncronas conforme el usuario escribe.
- **Resultados Dinámicos**: Muestra hasta 10 resultados con arte original y audios listos para reproducir.
- **Gestión de Estados**: Maneja estados de carga (`isSearching`) y limpieza automática de resultados.

### C. Controles de Reproducción y Lógica de Cola
El reproductor (`MediaPlayer`) ha sido potenciado con:
- **Navegación de Pistas**: Botones de "Siguiente" y "Anterior" que funcionan basados en el contexto de la lista actual (Biblioteca, Álbum o Resultados de Búsqueda).
- **Reproducción Automática**: Al finalizar una pista (30 segundos), la App selecciona y reproduce automáticamente el siguiente tema de la lista.
- **Mini-Reproductor Persistente**: Control básico visible en toda la aplicación mientras se navega entre pantallas.

---

## 4. Funcionamiento de la API de iTunes
- **Fuente**: Se utiliza `https://itunes.apple.com/search`.
- **Audio**: Se obtienen `previewUrl` que son archivos MP3 de alta calidad.
- **Limitación de 30 Segundos**: Los audios tienen esta duración debido a las políticas de copyright de Apple para desarrolladores. Es el estándar legal para demostraciones de streaming sin licencias comerciales.
- **Imágenes**: Se obtienen portadas oficiales y se procesan para mostrarse en resolución HD (600x600).

---

## 5. Gestión de Navegación
La App utiliza un `BottomBar` conectado al `NavHost`, permitiendo saltar entre el Inicio, el Buscador y la Biblioteca de forma instantánea mientras la música sigue sonando en segundo plano gracias al estado global del `MusicViewModel`.
