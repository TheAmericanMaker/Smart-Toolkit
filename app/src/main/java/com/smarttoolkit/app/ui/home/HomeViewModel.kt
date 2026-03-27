package com.smarttoolkit.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttoolkit.app.data.model.UtilityItem
import com.smarttoolkit.app.data.model.allUtilities
import com.smarttoolkit.app.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val utilities: List<UtilityItem> = allUtilities,
    val favorites: List<String> = emptyList(),
    val searchQuery: String = "",
    val adsRemoved: Boolean = false
) {
    val filteredUtilities: List<UtilityItem>
        get() = if (searchQuery.isBlank()) utilities
        else utilities.filter { it.name.contains(searchQuery, ignoreCase = true) }

    val favoriteUtilities: List<UtilityItem>
        get() {
            val utilityMap = utilities.associateBy { it.id }
            return favorites.mapNotNull { utilityMap[it] }
        }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<HomeUiState> = combine(
        preferencesRepository.favorites,
        _searchQuery,
        preferencesRepository.adsRemoved
    ) { favorites, query, adsRemoved ->
        HomeUiState(
            utilities = allUtilities,
            favorites = favorites,
            searchQuery = query,
            adsRemoved = adsRemoved
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(utilityId: String) {
        viewModelScope.launch {
            preferencesRepository.toggleFavorite(utilityId)
        }
    }

    fun reorderFavorite(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            preferencesRepository.reorderFavorite(fromIndex, toIndex)
        }
    }
}
