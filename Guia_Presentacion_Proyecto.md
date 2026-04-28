# 🎙️ Guía de Presentación: Proyecto Spotify Clone

Este documento contiene la información clave para explicar el funcionamiento, arquitectura y tecnologías utilizadas en el proyecto.

## 1. Propósito del Proyecto
Desarrollar un clon funcional de la interfaz de **Spotify** para Android, enfocado en la experiencia de usuario (UX/UI) y el manejo de flujos de navegación y estados de autenticación.

## 2. Tecnologías Utilizadas (Stack Tecnológico)
*   **Lenguaje:** Kotlin (100%).
*   **UI Framework:** Jetpack Compose (Declarativo).
*   **Arquitectura:** MVVM (Model-View-ViewModel).
*   **Navegación:** Navigation Component para Compose.
*   **Manejo de Estado:** StateFlow y ViewModel (Arquitectura recomendada por Google).
*   **Diseño:** Material Design 3.

## 3. Arquitectura (MVVM)
*   **Model (Modelo):** Clases de datos que representan la información (ej. `Song.kt`, `User.kt`).
*   **View (Vista):** Composables que definen la interfaz. No contienen lógica, solo reaccionan al estado (ej. `HomeScreen.kt`, `LoginEmailScreen.kt`).
*   **ViewModel:** `AuthViewModel.kt`. Actúa como puente entre los datos y la vista. Mantiene la información del usuario de forma persistente durante el ciclo de vida de la app.

## 4. Componentes Clave de la UI
*   **Scaffold:** Estructura básica que organiza el TopBar, BottomBar y el contenido.
*   **LazyColumn / LazyRow:** Listas optimizadas que solo renderizan los elementos visibles (similar al RecyclerView, pero más moderno).
*   **Gradientes (Brush):** Implementados en `AlbumDetailScreen` y `LikedSongsScreen` para replicar la estética de Spotify.
*   **Navegación:** Centralizada en `NavGraph.kt` usando un `NavHost` para gestionar el intercambio de pantallas.

## 5. Lógica de Autenticación
*   La app simula un sistema de registro y login. 
*   Los usuarios se guardan en una **lista temporal en memoria** dentro del `AuthViewModel`.
*   Esto permite probar el flujo completo: Registrarse -> Ir al Login -> Validar credenciales -> Entrar al Home.

## 6. Preguntas Frecuentes del Profesor (Tips)
*   **¿Por qué Compose y no XML?** Porque Compose permite una interfaz más dinámica, con menos código y es el estándar actual de la industria.
*   **¿Qué es el StateFlow?** Es un flujo de datos que emite actualizaciones a la interfaz. Si los datos cambian, la UI se "recompone" (se actualiza) sola.
*   **¿Cómo manejas la navegación?** Mediante una `Sealed Class Screen` que define rutas únicas, evitando errores de escritura al movernos entre pantallas.

---
*Este archivo fue generado para el avance del proyecto final de Desarrollo de Aplicaciones Móviles.*
