package com.sportswipe.shared.domain.repository

import com.sportswipe.shared.domain.model.DiscoverItem
import com.sportswipe.shared.domain.model.Match
import com.sportswipe.shared.domain.model.Message
import com.sportswipe.shared.domain.model.MyProfile
import com.sportswipe.shared.domain.model.Objective
import com.sportswipe.shared.domain.model.Profile
import com.sportswipe.shared.domain.model.Settings
import com.sportswipe.shared.domain.model.Sport
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun seedIfNeeded()
    fun discoverItems(): Flow<List<DiscoverItem>>
    fun likesReceived(): Flow<List<Profile>>
    fun recommendedTop10(): Flow<List<Profile>>
    suspend fun relatedByGroup(group: Objective): List<Profile>
    suspend fun like(profileId: Long): Boolean
    suspend fun nope(profileId: Long)
    suspend fun rewind()
    suspend fun getProfile(profileId: Long): Profile?
}

interface MatchRepository {
    fun matches(): Flow<List<Match>>
    suspend fun getMatch(matchId: Long): Match?
    suspend fun createMatch(profileId: Long): Match?
}

interface MessageRepository {
    fun messages(matchId: Long): Flow<List<Message>>
    suspend fun send(matchId: Long, body: String)
}

interface SettingsRepository {
    fun settings(): Flow<Settings>
    suspend fun setPremium(isPremium: Boolean)
    suspend fun setAdFrequency(n: Int)
    suspend fun setFreeMatchLimit(limit: Int)
}

interface MyProfileRepository {
    fun myProfile(): Flow<MyProfile>
    suspend fun save(profile: MyProfile)
    suspend fun deactivate()
    suspend fun deleteAllData()
}
