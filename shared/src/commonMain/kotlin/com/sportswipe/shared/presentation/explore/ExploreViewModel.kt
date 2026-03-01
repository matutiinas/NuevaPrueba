package com.sportswipe.shared.presentation.explore

import com.sportswipe.shared.domain.model.Objective
import com.sportswipe.shared.domain.model.Profile
import com.sportswipe.shared.domain.repository.ProfileRepository
import com.sportswipe.shared.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ExploreUiState(
    val recommended: List<Profile> = emptyList(),
    val group: Objective? = null,
    val groupProfiles: List<Profile> = emptyList(),
)

class ExploreViewModel(private val profileRepository: ProfileRepository) : BaseViewModel() {
    private val _state = MutableStateFlow(ExploreUiState())
    val state: StateFlow<ExploreUiState> = _state.asStateFlow()

    init {
        scope.launch { _state.value = _state.value.copy(recommended = profileRepository.recommendedTop10().first()) }
    }

    fun openGroup(group: Objective) {
        scope.launch {
            _state.value = _state.value.copy(group = group, groupProfiles = profileRepository.relatedByGroup(group))
        }
    }
}
