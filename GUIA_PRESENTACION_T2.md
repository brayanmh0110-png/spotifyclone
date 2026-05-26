# Guía de Presentación T2: Preguntas y Cambios Probables

Esta guía te ayudará a responder las preguntas técnicas de tu profesor y a realizar cambios en vivo si te lo solicita.

## 1. Preguntas sobre Arquitectura

**P: ¿Por qué usaste MVVM y no pusiste la lógica directamente en las pantallas?**
> *R: Para separar responsabilidades. La UI (Compose) solo se encarga de mostrar datos. El ViewModel gestiona el estado y la lógica, y el Repositorio maneja los datos. Esto hace que el código sea fácil de testear y mantener.*

**P: ¿Cómo se comunican los datos entre el Repositorio y la UI?**
> *R: Usamos `StateFlow` y `Flow`. El Repositorio devuelve un flujo de datos de Firestore, el ViewModel lo recolecta y lo expone como un estado reactivo que la UI observa.*

---

## 2. Preguntas sobre la API y Datos

**P: ¿De dónde salen las 50 canciones? ¿Están guardadas en el código?**
> *R: No están en el código. Al abrir la app, la función `seedFullProjectData` en `MusicRepository` las busca en la API de iTunes y las guarda en Firebase Firestore. La app solo lee de Firestore.*

**P: ¿Por qué el audio dura solo 30 segundos?**
> *R: Es una limitación legal de la iTunes API para desarrolladores (Previews). Permite demostrar el streaming sin infringir derechos de autor.*

---

## 3. Cambios en vivo (Lo que el profesor podría pedir)

### Solicitud: "Cambia el número de resultados de búsqueda de 10 a 5"
- **Dónde ir:** `MusicRepository.kt` -> Función `searchSongs`.
- **Qué cambiar:** Cambia `limit=10` por `limit=5` en la URL.

### Solicitud: "Cambia el color principal (Verde Spotify) por otro"
- **Dónde ir:** `ui/theme/Color.kt`.
- **Qué cambiar:** Cambia el valor de `val Green = Color(0xFF1DB954)` por otro código hexadecimal (ej: Azul `0xFF1D70B9`).

### Solicitud: "Añade un nuevo género musical"
- **Dónde ir:** `MusicRepository.kt` -> Función `seedFullProjectData`.
- **Qué cambiar:** Añade un nuevo objeto `Genre` a la lista `genres` y un nuevo set de canciones a la lista de `queries`.

---

## 4. Conceptos Clave que DEBES dominar
1. **Corrutinas (`suspend`, `async`):** Se usan para que la app no se congele mientras descarga música o consulta la base de datos.
2. **Jetpack Compose:** Interfaz declarativa. En lugar de modificar vistas, "describimos" cómo debe verse el estado actual.
3. **Firestore:** Base de datos NoSQL basada en Documentos y Colecciones.
4. **MediaPlayer:** Clase nativa de Android para gestionar audio (preparar, iniciar, pausar).

**¡Mucha suerte en tu entrega mañana! El proyecto está sólido.**
