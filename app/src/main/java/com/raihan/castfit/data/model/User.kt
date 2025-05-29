package com.raihan.castfit.data.model

import com.google.firebase.auth.FirebaseUser

data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val age: Int? = null,
    val dateOfBirth: String? = null
){
    constructor() : this("", "", "", null, null)
}

fun FirebaseUser?.toUser() =
    this?.let {
        User(
            id = this.uid,
            fullName = this.displayName.orEmpty(),
            email = this.email.orEmpty(),
        )
    }

