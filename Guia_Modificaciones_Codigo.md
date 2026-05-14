# 🛠️ Guía de Modificaciones Rápidas (Arquitectura Firebase)

Con la llegada de Firebase y el Patrón Repositorio, el código está más organizado. Aquí tienes cómo modificarlo:

## 1. Modificar Lógica de Autenticación
*   **¿Dónde cambiar validaciones?**: Ve a `AuthRepository.kt`. 
    *   Busca `validateEmail` o `validatePassword`. 
    *   Si el profesor pide que la clave tenga 10 caracteres, cambia el `6` por `10` en `password.length >= 6`.
*   **¿Dónde se guardan los usuarios?**: En `AuthRepository.kt`, dentro de la función `register`. Ahí verás la colección `"users"` de Firestore.

## 2. Agregar un nuevo campo al Perfil (Ej: Edad)
1.  **Modelo**: En `User.kt`, añade `val age: Int = 0`.
2.  **Repositorio**: En `AuthRepository.kt`, actualiza la función `register` para incluir el nuevo campo en el objeto `User` que se envía a Firestore.
3.  **UI**: En `RegisterScreen.kt`, añade el nuevo `TextField` y conéctalo mediante el ViewModel.

## 3. Manejo de Errores (Toasts y Mensajes)
*   **¿Cómo mostrar errores?**: El `AuthViewModel` ahora tiene un `StateFlow` llamado `error`.
*   En las pantallas (`RegisterScreen` o `LoginEmailScreen`), puedes observar este error y mostrar un `Toast` si no es nulo:
    ```kotlin
    val error by authViewModel.error.collectAsState()
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            authViewModel.clearError()
        }
    }
    ```

## 4. Persistencia con DataStore
*   **¿Cómo cambiar qué se guarda?**: Ve a `UserPreferencesRepository.kt`. 
*   Aquí se define el nombre del archivo de preferencias y las llaves (keys) como `USER_UID`.

## 5. Cambiar el Diseño de las Listas
*   **Canciones Favoritas**: Sigue estando en `LikedSongsScreen.kt`. Si te pide que los datos vengan de la nube, tendrías que crear un nuevo repositorio para canciones similar al de `Auth`.

## 6. Trucos de Debugging (Firebase)
*   **Firebase Console**: Si algo no funciona, entra a la consola de Firebase en la web para ver si los usuarios se están creando en la pestaña "Authentication" o si los datos llegan a "Firestore Database".
*   **Errores de red**: Recuerda que ahora la app necesita internet para loguearse. Si falla, verifica la conexión.

## 7. Comandos y Atajos (Recordatorio)
*   **`Ctrl + Shift + F`**: Buscar en todo el proyecto (útil para buscar dónde se usa `"users"`).
*   **`Ctrl + B`**: Ir a la definición de una función (muy útil para saltar del ViewModel al Repositorio).

---
**Recuerda:** Ahora que usas Repositorios, la regla de oro es: 
1. La **UI** solo pide cosas. 
2. El **ViewModel** gestiona el estado (cargando, error). 
3. El **Repository** hace el trabajo sucio con Firebase.
