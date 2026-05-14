# 🎙️ Guía de Presentación Actualizada: Proyecto Spotify Clone (con Firebase)

Este documento contiene la información clave para explicar la integración de Firebase y la arquitectura avanzada del proyecto.

## 1. Propósito y Nuevas Funcionalidades
La aplicación ahora cuenta con un sistema de **backend real**:
*   **Autenticación en la Nube**: Registro e inicio de sesión gestionados por **Firebase Auth**.
*   **Base de Datos en Tiempo Real**: Perfiles de usuario almacenados en **Cloud Firestore**.
*   **Persistencia de Sesión**: Uso de **Jetpack DataStore** para que el usuario no tenga que loguearse cada vez que abre la app.

## 2. Tecnologías y Librerías (Stack Actualizado)
*   **Firebase Authentication**: Manejo seguro de emails y contraseñas.
*   **Cloud Firestore**: Base de Datos NoSQL para guardar nombres y datos de usuario.
*   **Jetpack DataStore**: Librería para guardar el UID del usuario de forma persistente.
*   **Kotlin Coroutines & Flow**: Para manejar procesos asíncronos y flujos de datos en tiempo real.
*   **Repository Pattern**: Capa intermedia que separa la fuente de datos (Firebase) de la lógica de negocio (ViewModel).

## 3. Arquitectura MVVM + Repository
*   **Model (`User.kt`)**: Representa al usuario con campos como `uid`, `email` y `name`.
*   **Repository (`AuthRepository.kt`)**: Contiene las llamadas directas a Firebase (`signInWithEmailAndPassword`, `createUserWithEmailAndPassword`).
*   **ViewModel (`AuthViewModel.kt`)**: Expone estados como `isLoggedIn` y `error`. No almacena contraseñas por seguridad, estas se pasan directamente del UI al Repositorio.
*   **View**: Pantallas que reaccionan a los cambios de estado del ViewModel mediante `collectAsState`.

## 4. Puntos Clave para Explicar (Tips)
*   **Seguridad**: *"No guardamos la contraseña en el modelo de datos. La capturamos en un estado local de la pantalla y la enviamos directamente al repositorio para que Firebase la procese."*
*   **Experiencia de Usuario**: *"Gracias a DataStore, la aplicación recuerda quién eres. Al abrir la app, el ViewModel comprueba la sesión automáticamente (`checkSession`)."*
*   **Manejo de Errores**: *"Si Firebase devuelve un error (ej. contraseña incorrecta), lo capturamos en el Repositorio y lo mostramos en la UI a través del Flow de error del ViewModel."*

---
*Este proyecto ahora es una aplicación profesional conectada a la nube con arquitectura recomendada por Google.*
