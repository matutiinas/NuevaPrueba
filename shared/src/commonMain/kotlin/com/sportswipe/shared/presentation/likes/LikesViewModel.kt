package com.sportswipe.shared.presentation.likes

import com.sportswipe.shared.domain.model.Profile
import com.sportswipe.shared.domain.repository.ProfileRepository
import com.sportswipe.shared.domain.repository.SettingsRepository
import com.sportswipe.shared.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class LikesUiState(val profiles: List<Profile> = emptyList(), val isPremium: Boolean = false)

class LikesViewModel(
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
) : BaseViewModel() {
    private val _state = MutableStateFlow(LikesUiState())
    val state: StateFlow<LikesUiState> = _state.asStateFlow()

    init {
        scope.launch {
            _state.value = LikesUiState(
                profiles = profileRepository.likesReceived().first(),
                isPremium = settingsRepository.settings().first().isPremium
            )
        }
    }
}
