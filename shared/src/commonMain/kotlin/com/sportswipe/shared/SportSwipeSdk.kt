package com.sportswipe.shared

import com.sportswipe.shared.data.local.DatabaseFactory
import com.sportswipe.shared.data.local.DriverFactory
import com.sportswipe.shared.data.repository.AppContainer
import com.sportswipe.shared.domain.usecase.LikeProfileUseCase
import com.sportswipe.shared.presentation.chats.ChatsViewModel
import com.sportswipe.shared.presentation.discover.DiscoverViewModel
import com.sportswipe.shared.presentation.explore.ExploreViewModel
import com.sportswipe.shared.presentation.likes.LikesViewModel
import com.sportswipe.shared.presentation.profile.ProfileViewModel

class SportSwipeSdk(driverFactory: DriverFactory) {
    private val container = AppContainer(DatabaseFactory(driverFactory))

    suspend fun init() = container.init()

    fun discoverViewModel() = DiscoverViewModel(
        container.profileRepository,
        container.matchRepository,
        container.settingsRepository,
        LikeProfileUseCase(container.profileRepository, container.matchRepository, container.settingsRepository),
    )

    fun likesViewModel() = LikesViewModel(container.profileRepository, container.settingsRepository)
    fun chatsViewModel() = ChatsViewModel(container.matchRepository, container.messageRepository)
    fun exploreViewModel() = ExploreViewModel(container.profileRepository)
    fun profileViewModel() = ProfileViewModel(container.myProfileRepository, container.settingsRepository)
}
