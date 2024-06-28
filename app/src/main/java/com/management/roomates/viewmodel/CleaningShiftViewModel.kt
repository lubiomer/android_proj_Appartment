package com.management.roomates.viewmodel

import android.content.Context
import androidx.lifecycle.*
import com.management.roomates.data.dao.CleaningShiftDao
import com.management.roomates.data.model.CleaningShift
import com.management.roomates.data.model.User
import com.management.roomates.repository.CleaningShiftRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CleaningShiftViewModel(
    private val cleaningShiftDao: CleaningShiftDao,
    private val currentUserId: String
) : ViewModel() {

    private val externalScope = CoroutineScope(Dispatchers.IO)
    private val cleaningShiftRepository: CleaningShiftRepository = CleaningShiftRepository(cleaningShiftDao, externalScope, currentUserId)

    val allCleaningShifts: LiveData<List<CleaningShift>> = cleaningShiftRepository.allCleaningShifts

    private val _currentCleaningShift = MutableLiveData<CleaningShift?>()
    val currentCleaningShift: LiveData<CleaningShift?> get() = _currentCleaningShift

    fun getUsersByApartmentId(apartmentId: String, callback: (List<User>) -> Unit) {
        cleaningShiftRepository.getUsersByApartmentId(apartmentId,callback)
    }

    fun insert(cleaningShift: CleaningShift) {
        viewModelScope.launch {
            cleaningShiftRepository.insert(cleaningShift)
        }
    }

    fun update(cleaningShift: CleaningShift) {
        viewModelScope.launch {
            cleaningShiftRepository.update(cleaningShift)
        }
    }

    fun delete(cleaningShift: CleaningShift) {
        viewModelScope.launch {
            cleaningShiftRepository.delete(cleaningShift)
        }
    }

    fun setCurrentCleaningShift(cleaningShift: CleaningShift?) {
        _currentCleaningShift.value = cleaningShift
    }

    fun clearCurrentCleaningShift() {
        _currentCleaningShift.value = null
    }
}

class CleaningShiftViewModelFactory(
    private val cleaningShiftDao: CleaningShiftDao,
    private val currentUserId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CleaningShiftViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CleaningShiftViewModel(cleaningShiftDao, currentUserId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
