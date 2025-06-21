package com.app.rehearsalcloud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.rehearsalcloud.repository.SetlistRepository

class SetlistViewModelFactory(
    private val repository: SetlistRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SetlistViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SetlistViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}