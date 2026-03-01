package com.sportswipe.shared.presentation.discover

import com.sportswipe.shared.domain.model.DiscoverItem
import com.sportswipe.shared.domain.model.Match
import com.sportswipe.shared.domain.model.Settings
import com.sportswipe.shared.domain.repository.MatchRepository
import com.sportswipe.shared.domain.repository.ProfileRepository
import com.sportswipe.shared.domain.repository.SettingsRepository
import com.sportswipe.shared.domain.usecase.LikeProfileUseCase
import com.sportswipe.shared.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class DiscoverUiState(
    val items: List<DiscoverItem> = emptyList(),
    val currentIndex: Int = 0,
    val currentMatch: Match? = null,
)

class DiscoverViewModel(
    private val profileRepository: ProfileRepository,
    private val matchRepository: MatchRepository,
    private val settingsRepository: SettingsRepository,
    private val likeProfileUseCase: LikeProfileUseCase,
) : BaseViewModel() {
    private val _state = MutableStateFlow(DiscoverUiState())
    val state: StateFlow<DiscoverUiState> = _state.asStateFlow()

    init {
        scope.launch {
            _state.value = _state.value.copy(items = profileRepository.discoverItems().first())
        }
    }

    fun likeCurrent() {
        scope.launch {
            val item = _state.value.items.getOrNull(_state.value.currentIndex) as? DiscoverItem.User ?: return@launch
            val settings = settingsRepository.settings().first()
            val currentMatches = matchRepository.matches().first().size
            val match = likeProfileUseCase(item.profile.id, currentMatches, settings)
            _state.value = _state.value.copy(currentIndex = _state.value.currentIndex + 1, currentMatch = match)
        }
    }

    fun nopeCurrent() {
        _state.value = _state.value.copy(currentIndex = _state.value.currentIndex + 1)
    }
}
