# Documentación Final del Proyecto: Spotify Clone (Listo para T2)

Este proyecto es una aplicación de Android de alto rendimiento inspirada en Spotify. Utiliza una arquitectura moderna, integración con servicios en la nube (Firebase) y consumo de datos reales mediante APIs externas.

## 1. Tecnologías y Herramientas
- **Lenguaje:** Kotlin (Corrutinas, Flow, Sealed Classes).
- **UI:** Jetpack Compose (Material 3) - Interfaz 100% declarativa y reactiva.
- **Arquitectura:** MVVM (Model-View-ViewModel) + Repository Pattern.
- **Backend:** 
  - **Firebase Auth:** Gestión de sesiones y seguridad de usuarios.
  - **Firestore:** Base de datos NoSQL para persistencia de música, perfiles y favoritos.
- **Multimedia:** Android MediaPlayer con soporte para streaming de audio.
- **API Externa:** iTunes Search API para obtención de metadatos, arte de álbum en HD y audio real.
- **Almacenamiento Local:** DataStore para persistencia de la sesión del usuario.

---

## 2. Estructura y Organización del Código

### Paquetes Principales
- **`model/`**: Entidades de datos (`Song`, `Album`, `Artist`, `User`, `Genre`). Diseñadas para ser escalables y compatibles con Firestore.
- **`repository/`**: 
  - `MusicRepository.kt`: Lógica de sincronización API-Firestore y consultas de música.
  - `AuthRepository.kt`: Gestión de credenciales y perfiles con Firebase.
- **`viewmodel/`**: 
  - `MusicViewModel.kt`: Gestiona la lógica del reproductor, la cola de reproducción y los resultados de búsqueda.
  - `AuthViewModel.kt`: Gestiona el estado de autenticación y la personalización del perfil (incluyendo foto).
- **`ui/`**: 
  - `screens/`: Pantallas completas (Home, Search, Library, Player, Panel Usuario, etc.).
  - `components/`: Elementos reutilizables como el `MiniPlayer` y `BottomNavigationBar`.
  - `theme/`: Definición de la identidad visual (Colores, Tipografía).

---

## 3. Funcionalidades Clave

### A. Sistema de Datos Dinámico (iTunes API + Firestore)
La aplicación no utiliza datos estáticos ("hardcoded"). Al iniciar, realiza un sembrado paralelo:
- Descarga 50 canciones reales de 5 géneros distintos.
- Vincula automáticamente artistas y álbumes.
- Guarda todo en Firestore para cumplir con la regla de base de datos propia.

### B. Buscador Global Inteligente
El buscador permite al usuario encontrar cualquier canción en el mundo en tiempo real:
- Consultas asíncronas a la API de iTunes.
- Resultados con arte original y audio listo para reproducir.

### C. Reproductor Avanzado
- **Streaming Real:** Reproduce archivos MP3 de alta calidad.
- **Lógica de Navegación:** Botones de Siguiente/Anterior con memoria de cola de reproducción.
- **Reproducción Automática:** Al terminar una canción, salta al siguiente tema de la lista actual.

### D. Gestión de Perfil de Usuario
- Registro e inicio de sesión seguro.
- **Foto de Perfil Dinámica:** Integración con la galería del dispositivo para seleccionar y guardar fotos de perfil personalizadas, persistidas en la base de datos.

---

## 4. Conclusiones Técnicas
- **Escalabilidad:** La separación por capas permite añadir nuevas funciones (como playlists propias) sin romper el código existente.
- **Rendimiento:** El uso de corrutinas (`async/awaitAll`) garantiza que la carga masiva de datos sea imperceptible para el usuario.
- **Legalidad:** Se utilizan Previews de 30 segundos cumpliendo con las licencias de desarrollador de Apple Music.
