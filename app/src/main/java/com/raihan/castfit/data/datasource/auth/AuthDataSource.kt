package com.raihan.castfit.data.datasource.auth

import com.google.firebase.auth.FirebaseAuth
import com.raihan.castfit.data.model.User
import com.raihan.castfit.data.model.toUser
import com.raihan.castfit.data.source.firebase.FirebaseService
import kotlinx.coroutines.tasks.await
import kotlin.jvm.Throws

interface AuthDataSource {
    @Throws(exceptionClasses = [Exception::class])
    suspend fun doLogin(
        email: String,
        password: String
    ): Boolean

    @Throws(exceptionClasses = [Exception::class])
    suspend fun doRegister(
        email: String,
        fullName: String,
        password: String
    ): Boolean

    suspend fun updateProfile(fullName: String? = null): Boolean

    suspend fun updatePassword(newPassword: String): Boolean

    suspend fun updateEmail(newEmail: String): Boolean

    suspend fun requestForgotPassword(email: String): Boolean

    suspend fun getUserDataFromFirestore(uid: String): User?

    fun requestChangePasswordByEmail(): Boolean

    fun doLogout(): Boolean

    fun isLoggedIn(): Boolean

    fun getCurrentUser(): User?
}

class FirebaseAuthDataSource(private val service: FirebaseService) : AuthDataSource {
    override suspend fun doLogin(
        email: String,
        password: String,
    ): Boolean {
        return service.doLogin(email, password)
    }

    override suspend fun doRegister(
        email: String,
        fullName: String,
        password: String,
    ): Boolean {
        return service.doRegister(email, fullName, password)
    }

    override suspend fun updateProfile(fullName: String?): Boolean {
        return service.updateProfile(fullName)
    }

    override suspend fun updatePassword(newPassword: String): Boolean {
        return service.updatePassword(newPassword)
    }

    override suspend fun updateEmail(newEmail: String): Boolean {
        return service.updateEmail(newEmail)
    }

    override fun requestChangePasswordByEmail(): Boolean {
        return service.requestChangePasswordByEmail()
    }

    override fun doLogout(): Boolean {
        return service.doLogout()
    }

    override fun isLoggedIn(): Boolean {
        return service.isLoggedIn()
    }

    override fun getCurrentUser(): User? {
        return service.getCurrentUser().toUser()
    }

    override suspend fun requestForgotPassword(email: String): Boolean {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
        return true
    }

    override suspend fun getUserDataFromFirestore(uid: String): User? {
        return service.getUserDataFromFirestore(uid)
    }

}