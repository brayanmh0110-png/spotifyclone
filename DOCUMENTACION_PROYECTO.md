# Documentación Final del Proyecto: Spotify Clone (Listo para T2)

Este proyecto es una aplicación de Android de alto rendimiento inspirada en Spotify. Utiliza una arquitectura moderna, integración con servicios en la nube (Firebase) y consumo de datos reales mediante APIs externas para ofrecer una experiencia 100% funcional.

## 1. Tecnologías y Herramientas
- **Lenguaje:** Kotlin (Corrutinas, Flow, Sealed Classes).
- **UI:** Jetpack Compose (Material 3) - Interfaz 100% declarativa y reactiva.
- **Arquitectura:** MVVM (Model-View-ViewModel) + Repository Pattern.
- **Backend:** 
  - **Firebase Auth:** Gestión de sesiones y seguridad de usuarios.
  - **Firestore:** Base de datos NoSQL para persistencia de música, perfiles y favoritos.
- **Multimedia:** Android MediaPlayer con soporte para streaming de audio y gestión de colas.
- **API Externa:** iTunes Search API para obtención de metadatos, arte de álbum en HD y audio real.
- **Almacenamiento Local:** DataStore para persistencia de la sesión del usuario.

---

## 2. Estructura y Organización del Código

### Paquetes Principales
- **`model/`**: Entidades de datos (`Song`, `Album`, `Artist`, `User`, `Genre`, `Playlist`). Diseñadas para ser escalables y compatibles con Firestore.
- **`repository/`**: 
  - `MusicRepository.kt`: Lógica de sincronización API-Firestore, gestión de playlists y consultas de música.
  - `AuthRepository.kt`: Gestión de credenciales y perfiles con Firebase.
- **`viewmodel/`**: 
  - `MusicViewModel.kt`: Gestiona la lógica del reproductor, la cola de reproducción, los resultados de búsqueda y las listas del usuario.
  - `AuthViewModel.kt`: Gestiona el estado de autenticación y la personalización del perfil.
- **`ui/`**: 
  - `screens/`: Pantallas completas (Home, Search, Library, Player, Panel Usuario, Crear Playlist, etc.).
  - `components/`: Elementos reutilizables como el `MiniPlayer` y `SpotifyBottomBar`.

---

## 3. Funcionalidades Clave

### A. Sembrado de Datos Paralelo (iTunes API + Firestore)
La aplicación rellena automáticamente su base de datos al iniciar:
- Descarga 50 canciones reales (Pop, Rock PE, Reggaetón, Salsa, Cumbia).
- Vincula automáticamente artistas y álbumes originales.
- Utiliza Corrutinas paralelas (`async/awaitAll`) para cargar todo en menos de 5 segundos.

### B. Sistema de Biblioteca Integral (RF10/RF11/RF12)
La pantalla de Biblioteca es un centro de control total:
- **Organización por Pestañas**: Canciones, Álbumes, Artistas y Playlists creadas.
- **Gestión de Playlists**: Crear nuevas listas, agregar canciones mediante un menú de opciones y **eliminar** playlists completas con sincronización en tiempo real con Firestore.
- **Acceso Directo a Favoritos**: Un ítem especial "Tus me gustas" integrado en la lista de canciones.

### C. Reproductor Profesional con Lógica de Cola
- **Controles Avanzados:** Botones de Siguiente/Anterior con memoria de la lista actual (Cola de Reproducción).
- **Modos de Reproducción:** Soporta Modo Aleatorio (Shuffle) y Modos de Repetición (Bucle/Una sola pista).
- **Reproducción Automática:** Al terminar una pista, la App selecciona el siguiente tema de la cola automáticamente.

### D. Gestión de Perfil de Usuario
- **Seguridad:** Registro e inicio de sesión con Firebase.
- **Personalización:** Cambio de foto de perfil desde la galería del dispositivo con persistencia en la nube.

---

## 4. Conclusiones Técnicas y Legalidad
- **Escalabilidad:** El sistema permite añadir géneros o funcionalidades (como descargar música) de forma modular.
- **Rendimiento:** Carga diferida de imágenes con Coil y recolección de estados con `collectAsState` para una UI fluida.
- **Licencia:** Se utilizan Previews de 30 segundos (estándar de la industria para desarrolladores) para cumplir con los derechos de autor.
