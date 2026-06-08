package com.example.spotifyclone.repository

import com.example.spotifyclone.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Esta clase es el "puente" entre la aplicación y los servicios de Firebase para usuarios.
 * Maneja tanto la seguridad (llaves/cuentas) como los datos personales (perfiles).
 */
class AuthRepository {
    // Obtenemos las herramientas de Firebase:
    // FirebaseAuth: Se encarga de las contraseñas y el acceso seguro.
    private val auth = FirebaseAuth.getInstance()
    // FirebaseFirestore: Es nuestra base de datos donde guardamos nombres, fotos y favoritos.
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Revisa si hay una sesión activa en este momento.
     * Devuelve verdadero si el usuario ya entró a su cuenta.
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Intenta entrar a la cuenta usando correo y contraseña.
     * Si todo sale bien, devuelve el ID único del usuario (su "huella digital" en el sistema).
     */
    suspend fun login(email: String, password: String): Result<String> {
        return try {
            // Le pedimos a Firebase que verifique si el correo y clave coinciden
            val result = auth.signInWithEmailAndPassword(email, password).await()
            // Si funciona, devolvemos el UID (ID de usuario)
            Result.success(result.user?.uid ?: "")
        } catch (e: Exception) {
            // Si falla (ej: clave mal escrita), devolvemos el error
            Result.failure(e)
        }
    }

    /**
     * Crea una cuenta nueva desde cero.
     * 1. Crea la cuenta en el sistema de seguridad (Auth).
     * 2. Crea una "ficha" con los datos del usuario en la base de datos (Firestore).
     */
    suspend fun register(name: String, email: String, password: String): Result<String> {
        return try {
            // Paso 1: Crear la cuenta en el sistema de seguridad de Firebase
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: ""
            
            // Paso 2: Crear el perfil del usuario para nuestra base de datos
            val user = User(uid = uid, email = email, name = name)
            // Guardamos esta "ficha" en la colección de "users" usando su UID como nombre del archivo
            firestore.collection("users").document(uid).set(user).await()
            
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Busca la información de un usuario (nombre, foto, etc.) usando su ID único.
     */
    suspend fun getUserProfile(uid: String): User? {
        return try {
            // Entramos a la "carpeta" de usuarios y buscamos el documento con ese ID
            val snapshot = firestore.collection("users").document(uid).get().await()
            // Convertimos ese documento de la nube en un objeto User de nuestra app
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null // Si hay error o no existe, no devolvemos nada
        }
    }

    /**
     * Actualiza la foto de perfil del usuario en la base de datos.
     */
    suspend fun updateProfilePicture(uid: String, photoUrl: String): Result<Unit> {
        return try {
            // Buscamos al usuario y solo cambiamos el campo de "photoUrl"
            firestore.collection("users").document(uid).update("photoUrl", photoUrl).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene el ID único del usuario que tiene la sesión abierta ahora mismo.
     */
    fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Verifica que el texto escrito tenga el formato de un correo (ej: algo@correo.com).
     */
    fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Reglas para una contraseña segura:
     * - Mínimo 8 letras.
     * - Al menos un número.
     * - Al menos un símbolo (ej: !, @, #).
     */
    fun validatePassword(password: String): Boolean {
        return password.length >= 8 &&
               password.any { it.isDigit() } &&
               password.any { !it.isLetterOrDigit() }
    }
}
