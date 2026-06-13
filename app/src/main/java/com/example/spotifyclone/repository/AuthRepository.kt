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
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Revisa si hay una sesión activa en este momento.
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Intenta entrar a la cuenta usando correo y contraseña.
     */
    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Crea una cuenta nueva desde cero en Auth y Firestore.
     */
    suspend fun register(name: String, email: String, password: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: ""
            val user = User(uid = uid, email = email, name = name)
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
            val snapshot = firestore.collection("users").document(uid).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Actualiza la foto de perfil del usuario en Firestore.
     */
    suspend fun updateProfilePicture(uid: String, photoUrl: String): Result<Unit> {
        return try {
            firestore.collection("users").document(uid).update("photoUrl", photoUrl).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza el nombre del usuario en Firestore.
     */
    suspend fun updateUserName(uid: String, newName: String): Result<Unit> {
        return try {
            firestore.collection("users").document(uid).update("name", newName).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina permanentemente la cuenta del usuario y sus datos.
     */
    suspend fun deleteAccount(uid: String): Result<Unit> {
        return try {
            firestore.collection("users").document(uid).delete().await()
            auth.currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }

    fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    fun validatePassword(password: String): Boolean {
        return password.length >= 8 &&
               password.any { it.isDigit() } &&
               password.any { !it.isLetterOrDigit() }
    }
}
