package com.raihan.castfit.presentation.splashscreen

import androidx.lifecycle.ViewModel
import com.raihan.castfit.data.repository.UserRepository

class SplashScreenViewModel (private val repository: UserRepository) : ViewModel() {
    fun isUserLoggedIn() = repository.isLoggedIn()
}