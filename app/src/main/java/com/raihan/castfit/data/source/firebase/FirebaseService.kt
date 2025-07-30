package com.raihan.castfit.data.source.firebase

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.raihan.castfit.data.model.User

import kotlinx.coroutines.tasks.await

interface FirebaseService {
    @Throws(exceptionClasses = [Exception::class])
    suspend fun doLogin(
        email: String,
        password: String,
    ): Boolean

    @Throws(exceptionClasses = [Exception::class])
    suspend fun doRegister(
        email: String,
        fullName: String,
        password: String,
    ): Boolean

    suspend fun updateProfile(fullName: String? = null): Boolean

    suspend fun updatePassword(newPassword: String): Boolean

    suspend fun updateEmail(newEmail: String): Boolean

    fun requestChangePasswordByEmail(): Boolean

    fun doLogout(): Boolean

    fun isLoggedIn(): Boolean

    fun getCurrentUser(): FirebaseUser?

    suspend fun getUserDataFromFirestore(uid: String): User?

}

class FirebaseServiceImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
    ) : FirebaseService {
    // Login dengan email dan password
    override suspend fun doLogin(
        email: String,
        password: String,
    ): Boolean {
        val loginResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        return loginResult.user != null
    }

    // Registrasi akun baru dan simpan data ke Firestore
    override suspend fun doRegister(email: String, fullName: String, password: String): Boolean {
        val registerResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = registerResult.user ?: return false

        // Update displayName di FirebaseAuth
        firebaseUser.updateProfile(
            userProfileChangeRequest {
                displayName = fullName
            }
        ).await()

        // Simpan data user ke Firestore
        val userMap = hashMapOf(
            "fullName" to fullName,
            "email" to email,
            "age" to 0,
            "dateOfBirth" to ""
        )
        firestore.collection("users").document(firebaseUser.uid).set(userMap).await()

        return true
    }

    // Update nama di FirebaseAuth dan Firestore
    override suspend fun updateProfile(fullName: String?): Boolean {
        val user = getCurrentUser() ?: return false
        if (fullName != null) {
            user.updateProfile(
                userProfileChangeRequest {
                    displayName = fullName
                }
            ).await()

            // Update juga di Firestore
            val updates = hashMapOf<String, Any>(
                "fullName" to fullName
            )
            firestore.collection("users").document(user.uid).update(updates).await()
        }
        return true
    }

    override suspend fun updatePassword(newPassword: String): Boolean {
        getCurrentUser()?.updatePassword(newPassword)?.await()
        return true
    }

    // Ganti password user saat ini
    override suspend fun updateEmail(newEmail: String): Boolean {
        getCurrentUser()?.verifyBeforeUpdateEmail(newEmail)?.await()
        return true
    }

    // Kirim email reset password
    override fun requestChangePasswordByEmail(): Boolean {
        getCurrentUser()?.email?.let {
            firebaseAuth.sendPasswordResetEmail(it)
        }
        return true
    }

    override fun doLogout(): Boolean {
        Firebase.auth.signOut()
        return true
    }

    override fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    // Ambil user yang sedang login
    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    // Ambil data user dari Firestore berdasarkan UID
    override suspend fun getUserDataFromFirestore(uid: String): User? {
        val doc = Firebase.firestore.collection("users").document(uid).get().await()
        return if (doc.exists()) {
            doc.toObject(User::class.java)
        } else null
    }
}