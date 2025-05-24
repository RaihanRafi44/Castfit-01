package com.raihan.castfit.presentation.activityuser

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.raihan.castfit.data.model.HistoryActivity
import com.raihan.castfit.data.model.ProgressActivity
import com.raihan.castfit.data.repository.HistoryActivityRepository
import com.raihan.castfit.data.repository.ProgressActivityRepository
import com.raihan.castfit.utils.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
class ActivityViewModel(
    private val progressRepository: ProgressActivityRepository
) : ViewModel() {

    // Add this to track delete operation result
    private val _deleteOperationResult = MutableLiveData<Boolean?>()
    val deleteOperationResult: LiveData<Boolean?> = _deleteOperationResult

    fun getAllProgress() = progressRepository.getUserProgressData().asLiveData(Dispatchers.IO)

    fun removeProgress(item: ProgressActivity) {
        viewModelScope.launch(Dispatchers.IO) {
            // Validasi ID sebelum delete
            if (item.id == null || item.id <= 0) {
                Log.e("ActivityViewModel", "Cannot delete: Invalid ID (${item.id})")
                _deleteOperationResult.postValue(false)
                return@launch
            }

            Log.d("ActivityViewModel", "Attempting to delete activity: ${item.physicalActivityName} with ID: ${item.id}")

            progressRepository.deleteProgress(item).collect { result ->
                when (result) {
                    is ResultWrapper.Success -> {
                        Log.d("ActivityViewModel", "Successfully deleted activity: ${item.physicalActivityName} with ID: ${item.id}")
                        _deleteOperationResult.postValue(true)
                    }
                    is ResultWrapper.Error -> {
                        Log.e("ActivityViewModel", "Failed to delete activity: ${item.physicalActivityName}", result.exception)
                        _deleteOperationResult.postValue(false)
                    }
                    is ResultWrapper.Loading -> {
                        Log.d("ActivityViewModel", "Deleting activity in progress...")
                    }
                    else -> {
                        Log.w("ActivityViewModel", "Unexpected result state: $result")
                        _deleteOperationResult.postValue(false)
                    }
                }
            }
        }
    }

    fun resetDeleteResult() {
        _deleteOperationResult.value = null
    }
}*/

class ActivityViewModel(
    private val progressRepository: ProgressActivityRepository,
    private val historyRepository: HistoryActivityRepository
) : ViewModel() {

    // Track delete operation result
    private val _deleteOperationResult = MutableLiveData<Boolean?>()
    val deleteOperationResult: LiveData<Boolean?> = _deleteOperationResult

    // Track finish operation result
    private val _finishOperationResult = MutableLiveData<Boolean?>()
    val finishOperationResult: LiveData<Boolean?> = _finishOperationResult

    // Track delete history operation result
    private val _deleteHistoryResult = MutableLiveData<Boolean?>()
    val deleteHistoryResult: LiveData<Boolean?> = _deleteHistoryResult

    fun getAllProgress() = progressRepository.getUserProgressData().asLiveData(Dispatchers.IO)

    fun getAllHistory() = historyRepository.getUserHistoryData().asLiveData(Dispatchers.IO)

    fun removeProgress(item: ProgressActivity) {
        viewModelScope.launch(Dispatchers.IO) {
            // Validasi ID sebelum delete
            if (item.id == null || item.id <= 0) {
                Log.e("ActivityViewModel", "Cannot delete: Invalid ID (${item.id})")
                _deleteOperationResult.postValue(false)
                return@launch
            }

            Log.d("ActivityViewModel", "Attempting to delete activity: ${item.physicalActivityName} with ID: ${item.id}")

            progressRepository.deleteProgress(item).collect { result ->
                when (result) {
                    is ResultWrapper.Success -> {
                        Log.d("ActivityViewModel", "Successfully deleted activity: ${item.physicalActivityName} with ID: ${item.id}")
                        _deleteOperationResult.postValue(true)
                    }
                    is ResultWrapper.Error -> {
                        Log.e("ActivityViewModel", "Failed to delete activity: ${item.physicalActivityName}", result.exception)
                        _deleteOperationResult.postValue(false)
                    }
                    is ResultWrapper.Loading -> {
                        Log.d("ActivityViewModel", "Deleting activity in progress...")
                    }
                    else -> {
                        Log.w("ActivityViewModel", "Unexpected result state: $result")
                        _deleteOperationResult.postValue(false)
                    }
                }
            }
        }
    }

    fun finishActivity(item: ProgressActivity) {
        viewModelScope.launch(Dispatchers.IO) {
            // Validasi ID sebelum finish
            if (item.id == null || item.id <= 0) {
                Log.e("ActivityViewModel", "Cannot finish: Invalid ID (${item.id})")
                _finishOperationResult.postValue(false)
                return@launch
            }

            Log.d("ActivityViewModel", "Attempting to finish activity: ${item.physicalActivityName} with ID: ${item.id}")

            try {
                // Buat data untuk history
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

                // Hitung durasi (contoh: dari dateStarted sampai sekarang)
                val duration = item.dateStarted?.let { item.startedAt?.let { it1 ->
                    calculateDuration(it,
                        it1
                    )
                } }

                // Implementasi createHistory
                if (duration != null) {
                    historyRepository.createHistory(
                        progress = item,
                        dateEnded = currentDate,
                        completedAt = currentTime,
                        duration = duration
                    ).collect { historyResult ->
                        when (historyResult) {
                            is ResultWrapper.Success -> {
                                Log.d("ActivityViewModel", "Successfully created history for: ${item.physicalActivityName}")

                                // Setelah berhasil buat history, hapus dari progress
                                progressRepository.deleteProgress(item).collect { deleteResult ->
                                    when (deleteResult) {
                                        is ResultWrapper.Success -> {
                                            Log.d("ActivityViewModel", "Successfully moved activity to history: ${item.physicalActivityName}")
                                            _finishOperationResult.postValue(true)
                                        }

                                        is ResultWrapper.Error -> {
                                            Log.e("ActivityViewModel", "Failed to delete from progress after creating history", deleteResult.exception)
                                            _finishOperationResult.postValue(false)
                                        }

                                        is ResultWrapper.Loading -> {
                                            Log.d("ActivityViewModel", "Deleting from progress...")
                                        }

                                        else -> {
                                            Log.w("ActivityViewModel", "Unexpected delete result: $deleteResult")
                                            _finishOperationResult.postValue(false)
                                        }
                                    }
                                }
                            }

                            is ResultWrapper.Error -> {
                                Log.e("ActivityViewModel", "Failed to create history", historyResult.exception)
                                _finishOperationResult.postValue(false)
                            }

                            is ResultWrapper.Loading -> {
                                Log.d("ActivityViewModel", "Creating history...")
                            }

                            else -> {
                                Log.w("ActivityViewModel", "Unexpected history result: $historyResult")
                                _finishOperationResult.postValue(false)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ActivityViewModel", "Error finishing activity", e)
                _finishOperationResult.postValue(false)
            }
        }
    }

    fun removeHistory(item: HistoryActivity) {
        viewModelScope.launch(Dispatchers.IO) {
            // Validasi ID sebelum delete
            if (item.id == null || item.id <= 0) {
                Log.e("ActivityViewModel", "Cannot delete history: Invalid ID (${item.id})")
                _deleteHistoryResult.postValue(false)
                return@launch
            }

            Log.d("ActivityViewModel", "Attempting to delete history with ID: ${item.id}")

            historyRepository.deleteHistory(item).collect { result ->
                when (result) {
                    is ResultWrapper.Success -> {
                        Log.d("ActivityViewModel", "Successfully deleted history with ID: ${item.id}")
                        _deleteHistoryResult.postValue(true)
                    }
                    is ResultWrapper.Error -> {
                        Log.e("ActivityViewModel", "Failed to delete history", result.exception)
                        _deleteHistoryResult.postValue(false)
                    }
                    is ResultWrapper.Loading -> {
                        Log.d("ActivityViewModel", "Deleting history in progress...")
                    }
                    else -> {
                        Log.w("ActivityViewModel", "Unexpected result state: $result")
                        _deleteHistoryResult.postValue(false)
                    }
                }
            }
        }
    }

    private fun calculateDuration(dateStarted: String, startedAt: String): Int {
        return try {
            val startDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .parse("$dateStarted $startedAt")
            val currentTime = Date()

            val diffInMillis = currentTime.time - (startDateTime?.time ?: 0)
            val diffInMinutes = (diffInMillis / (1000 * 60)).toInt()

            Log.d("ActivityViewModel", "Calculated duration: $diffInMinutes minutes")
            diffInMinutes
        } catch (e: Exception) {
            Log.e("ActivityViewModel", "Error calculating duration", e)
            0 // Default duration jika ada error
        }
    }

    fun resetDeleteResult() {
        _deleteOperationResult.value = null
    }

    fun resetFinishResult() {
        _finishOperationResult.value = null
    }

    fun resetDeleteHistoryResult() {
        _deleteHistoryResult.value = null
    }
}
