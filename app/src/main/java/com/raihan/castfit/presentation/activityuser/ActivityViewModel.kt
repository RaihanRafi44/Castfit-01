package com.raihan.castfit.presentation.activityuser

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.raihan.castfit.data.model.ProgressActivity
import com.raihan.castfit.data.repository.ProgressActivityRepository
import com.raihan.castfit.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ActivityViewModel(
    private val progressRepository: ProgressActivityRepository
) : ViewModel() {
    fun getAllProgress() = progressRepository.getUserProgressData().asLiveData(Dispatchers.IO)

    fun removeProgress(item: ProgressActivity) {
        viewModelScope.launch(Dispatchers.IO) {
            progressRepository.deleteProgress(item).collect()
        }
    }
}