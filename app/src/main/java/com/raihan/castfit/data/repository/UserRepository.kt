package com.raihan.castfit.data.repository

import com.raihan.castfit.data.model.User
import com.raihan.castfit.utils.ResultWrapper
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    @Throws(exceptionClasses = [Exception::class])
    fun doLogin(
        email: String,
        password: String,
    ): Flow<ResultWrapper<Boolean>>

    @Throws(exceptionClasses = [Exception::class])
    fun doRegister(
        email: String,
        fullName: String,
        password: String,
    ): Flow<ResultWrapper<Boolean>>

    fun updateProfile(fullName: String? = null): Flow<ResultWrapper<Boolean>>

    fun updatePassword(newPassword: String): Flow<ResultWrapper<Boolean>>

    fun updateEmail(newEmail: String): Flow<ResultWrapper<Boolean>>

    fun requestChangePasswordByEmail(): Boolean

    fun doLogout(): Boolean

    fun isLoggedIn(): Boolean

    fun getCurrentUser(): User?

    fun requestForgotPassword(email: String): Flow<ResultWrapper<Boolean>>

    fun getUserData(): Flow<ResultWrapper<User>>

    fun getUserDateOfBirthAndAge(): Flow<ResultWrapper<Pair<String?, Int?>>>
    fun updateUserDateOfBirthAndAge(dob: String, age: Int): Flow<ResultWrapper<Boolean>>


}