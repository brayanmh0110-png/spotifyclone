# Guía de Presentación T2: Preguntas y Cambios Probables (Actualizada)

Esta guía te ayudará a responder las preguntas técnicas de tu profesor y a realizar cambios en vivo sobre las funciones de música y playlists.

## 1. Preguntas sobre Playlists y Datos

**P: ¿Cómo se guardan las playlists que crea el usuario?**
> *R: Se guardan en una colección llamada `playlists` en Firestore. Cada documento de playlist tiene un campo `ownerId` (el ID del usuario) y un array `songsIds` con los IDs de las canciones agregadas.*

**P: ¿Cómo haces para que la música siga sonando al cambiar de pestaña?**
> *R: El estado del reproductor y la instancia del `MediaPlayer` están en el `MusicViewModel`. Como el ViewModel vive mientras la actividad esté abierta, el audio no se interrumpe aunque el usuario navegue entre el Inicio, Buscar o la Biblioteca.*

**P: ¿Cómo implementaste la eliminación de una playlist?**
> *R: En el repositorio usamos `.delete()` sobre el documento de la playlist en Firestore. Antes de borrar, verificamos en el ViewModel que el usuario actual sea el dueño (`ownerId`).*

---

## 2. Cambios en vivo (Lo que el profesor podría pedir)

### Solicitud: "Haz que al buscar solo salgan 3 canciones"
- **Dónde ir:** `MusicRepository.kt` -> Función `searchSongs`.
- **Qué cambiar:** Cambia `limit=10` por `limit=3`.

### Solicitud: "Quita la opción de 'Agregar a la cola' de la Biblioteca"
- **Dónde ir:** `ui/screens/LibraryScreen.kt` -> Componente `ItemCancionBiblioteca`.
- **Qué cambiar:** Comenta o borra el primer `DropdownMenuItem` del menú.

### Solicitud: "Cambia el icono de la pestaña Biblioteca"
- **Dónde ir:** `ui/components/BottomNavigationBar.kt` (o donde esté definido el BottomBar).
- **Qué cambiar:** Busca `Icons.Default.LibraryMusic` y cámbialo por `Icons.Default.List`.

---

## 3. Lógica de Negocio (Para sustentar)
- **Cola de Reproducción:** Al hacer clic en una canción dentro de una lista, pasamos la lista completa (`playlist`) a la función `playSong`. Así, el ViewModel sabe qué canción sigue después de la actual.
- **Diferencia entre Favoritos y Playlist:** Favoritos es un array dentro del documento del **Usuario**. Las Playlists son documentos independientes en su propia **Colección**.

**¡Mucha suerte! Dominas el 100% de la lógica de red, base de datos y UI.**
