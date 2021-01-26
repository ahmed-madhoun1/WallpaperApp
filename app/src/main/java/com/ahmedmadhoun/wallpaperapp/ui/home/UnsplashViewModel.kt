package com.ahmedmadhoun.wallpaperapp.ui.home

import androidx.hilt.Assisted
import androidx.lifecycle.ViewModel
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.ahmedmadhoun.wallpaperapp.repository.UnsplashRepository

class UnsplashViewModel @ViewModelInject constructor(
    private val repository: UnsplashRepository,
    @Assisted state: SavedStateHandle
) : ViewModel() {

    private val searchValue = state.getLiveData(CURRENT_QUERY, DEFAULT_QUERY)

    val photos = searchValue.switchMap { query ->
        repository.getSearchResult(query = query).cachedIn(viewModelScope)
    }

    fun searchPhotos(query: String) {
        searchValue.value = query
    }

    companion object {
        private const val CURRENT_QUERY = "current_query"
        private const val DEFAULT_QUERY = "cats"
    }

}