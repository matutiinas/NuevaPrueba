package com.sportswipe.shared.domain

import com.sportswipe.shared.domain.model.Objective
import com.sportswipe.shared.domain.model.Profile
import com.sportswipe.shared.domain.model.Settings
import com.sportswipe.shared.domain.model.SocialLinks
import com.sportswipe.shared.domain.model.Sport
import com.sportswipe.shared.domain.repository.MatchRepository
import com.sportswipe.shared.domain.repository.ProfileRepository
import com.sportswipe.shared.domain.repository.SettingsRepository
import com.sportswipe.shared.domain.usecase.BuildPartnerWorkoutPlanUseCase
import com.sportswipe.shared.domain.usecase.LikeProfileUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest

class UseCasesTest {
    @Test
    fun buildWorkoutPlan_generatesExercises() {
        val useCase = BuildPartnerWorkoutPlanUseCase()
        val profile = sampleProfile(2)
        val plan = useCase(null, profile)
        assertTrue(plan.exercises.isNotEmpty())
        assertTrue(plan.durationMinutes >= 35)
    }

    @Test
    fun likeProfile_respectsFreeLimit() = runTest {
        val useCase = LikeProfileUseCase(FakeProfileRepository(), FakeMatchRepository(), FakeSettingsRepository())
        val result = useCase(10, currentMatches = 10, settings = Settings(isPremium = false, freeMatchLimit = 10))
        assertEquals(null, result)
    }

    private fun sampleProfile(id: Long) = Profile(
        id = id,
        name = "Alex",
        age = 25,
        distanceKm = 4,
        sports = listOf(Sport.GYM),
        objective = Objective.GYMBRO,
        bio = "test",
        photos = listOf("a", "b"),
        activityTags = emptyList(),
        verified = true,
        socialLinks = SocialLinks(true, true, false),
        marks = emptyMap(),
    )
}

private class FakeProfileRepository : ProfileRepository {
    override suspend fun seedIfNeeded() {}
    override fun discoverItems() = MutableStateFlow(emptyList<com.sportswipe.shared.domain.model.DiscoverItem>())
    override fun likesReceived() = MutableStateFlow(emptyList<Profile>())
    override fun recommendedTop10() = MutableStateFlow(emptyList<Profile>())
    override suspend fun relatedByGroup(group: Objective) = emptyList<Profile>()
    override suspend fun like(profileId: Long): Boolean = true
    override suspend fun nope(profileId: Long) {}
    override suspend fun rewind() {}
    override suspend fun getProfile(profileId: Long): Profile? = null
}

private class FakeMatchRepository : MatchRepository {
    override fun matches() = MutableStateFlow(emptyList<com.sportswipe.shared.domain.model.Match>())
    override suspend fun getMatch(matchId: Long) = null
    override suspend fun createMatch(profileId: Long) = null
}

private class FakeSettingsRepository : SettingsRepository {
    override fun settings(): Flow<Settings> = MutableStateFlow(Settings())
    override suspend fun setPremium(isPremium: Boolean) {}
    override suspend fun setAdFrequency(n: Int) {}
    override suspend fun setFreeMatchLimit(limit: Int) {}
}
