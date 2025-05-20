package com.raihan.castfit.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raihan.castfit.data.repository.UserRepository
import com.raihan.castfit.utils.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository) : ViewModel() {
    private val _loginResult = MutableLiveData<ResultWrapper<Boolean>>()
    val loginResult: LiveData<ResultWrapper<Boolean>>
        get() = _loginResult

    fun doLogin(
        email: String,
        password: String
    ){
        viewModelScope.launch(Dispatchers.IO){
            repository.doLogin(email, password).collect{
                _loginResult.postValue(it)
            }
        }
    }
}