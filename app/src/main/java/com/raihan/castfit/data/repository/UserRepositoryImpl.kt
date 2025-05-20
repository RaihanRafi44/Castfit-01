package com.raihan.castfit.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.raihan.castfit.data.datasource.auth.AuthDataSource
import com.raihan.castfit.data.model.User
import com.raihan.castfit.utils.ResultWrapper
import com.raihan.castfit.utils.proceedFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
    private val dataSource: AuthDataSource,
) :
    UserRepository {
    override fun doLogin(
        email: String,
        password: String,
    ): Flow<ResultWrapper<Boolean>> {
        return proceedFlow { dataSource.doLogin(email, password) }
    }

    override fun doRegister(
        email: String,
        fullName: String,
        password: String,
    ): Flow<ResultWrapper<Boolean>> {
        return proceedFlow { dataSource.doRegister(email, fullName, password) }
    }

    override fun updateProfile(fullName: String?): Flow<ResultWrapper<Boolean>> {
        return proceedFlow { dataSource.updateProfile(fullName = fullName) }
    }

    override fun updatePassword(newPassword: String): Flow<ResultWrapper<Boolean>> {
        return proceedFlow { dataSource.updatePassword(newPassword) }
    }

    override fun updateEmail(newEmail: String): Flow<ResultWrapper<Boolean>> {
        return proceedFlow { dataSource.updateEmail(newEmail) }
    }

    override fun requestChangePasswordByEmail(): Boolean {
        return dataSource.requestChangePasswordByEmail()
    }

    override fun doLogout(): Boolean {
        return dataSource.doLogout()
    }

    override fun isLoggedIn(): Boolean {
        return dataSource.isLoggedIn()
    }

    override fun getCurrentUser(): User? {
        return dataSource.getCurrentUser()
    }

    override fun requestForgotPassword(email: String): Flow<ResultWrapper<Boolean>> {
        return proceedFlow { dataSource.requestForgotPassword(email) }
    }

    override fun getUserData(): Flow<ResultWrapper<User>> {
        return proceedFlow {
            val uid = dataSource.getCurrentUser()?.id ?: throw Exception("User not logged in")
            dataSource.getUserDataFromFirestore(uid) ?: throw Exception("User data not found in Firestore")
        }
    }

    override fun getUserDateOfBirthAndAge(): Flow<ResultWrapper<Pair<String?, Int?>>> {
        Log.d("RepositoryDebug", "getUserDateOfBirthAndAge() DIPANGGIL")
        return proceedFlow {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
                ?: throw IllegalStateException("User not logged in")

            Log.d("RepositoryDebug", "UID: $uid")

            val doc = Firebase.firestore.collection("users").document(uid).get().await()

            Log.d("RepositoryDebug", "Dokumen ada? ${doc.exists()} | Data: ${doc.data}")

            val dob = doc.getString("dateOfBirth")
            val age = doc.getLong("age")?.toInt()

            Log.d("RepositoryDebug", "DOB: $dob, AGE: $age")

            Pair(dob, age)
        }
    }


    override fun updateUserDateOfBirthAndAge(dob: String, age: Int): Flow<ResultWrapper<Boolean>> {
        return proceedFlow {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
                ?: throw IllegalStateException("User not logged in")
            val data = mapOf("dateOfBirth" to dob, "age" to age)
            Firebase.firestore.collection("users").document(uid).update(data).await()
            true
        }
    }


}