package com.raihan.castfit.presentation.main

import androidx.lifecycle.ViewModel
import com.raihan.castfit.data.repository.UserRepository

class MainViewModel (
    private val userRepository: UserRepository,
) : ViewModel() {
    fun isLoggedIn() =
        userRepository
            .isLoggedIn()
}