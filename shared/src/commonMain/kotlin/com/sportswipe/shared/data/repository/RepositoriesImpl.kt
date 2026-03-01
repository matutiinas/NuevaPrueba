package com.sportswipe.shared.data.repository

import com.sportswipe.shared.data.local.DatabaseFactory
import com.sportswipe.shared.data.mapper.matchFromRaw
import com.sportswipe.shared.data.mapper.messageFromRaw
import com.sportswipe.shared.data.mapper.myProfileFromRaw
import com.sportswipe.shared.data.mapper.myProfileToRaw
import com.sportswipe.shared.data.mapper.profileFromRaw
import com.sportswipe.shared.data.mapper.serialize
import com.sportswipe.shared.data.seed.FakeDataSeeder
import com.sportswipe.shared.db.SportSwipeDatabase
import com.sportswipe.shared.domain.model.AdProfile
import com.sportswipe.shared.domain.model.DiscoverItem
import com.sportswipe.shared.domain.model.Match
import com.sportswipe.shared.domain.model.Message
import com.sportswipe.shared.domain.model.MyProfile
import com.sportswipe.shared.domain.model.Objective
import com.sportswipe.shared.domain.model.Profile
import com.sportswipe.shared.domain.model.Sender
import com.sportswipe.shared.domain.model.Settings
import com.sportswipe.shared.domain.repository.MatchRepository
import com.sportswipe.shared.domain.repository.MessageRepository
import com.sportswipe.shared.domain.repository.MyProfileRepository
import com.sportswipe.shared.domain.repository.ProfileRepository
import com.sportswipe.shared.domain.repository.SettingsRepository
import com.sportswipe.shared.domain.usecase.BuildPartnerWorkoutPlanUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class AppContainer(databaseFactory: DatabaseFactory) {
    private val db = databaseFactory.db
    private val settingsState = MutableStateFlow(Settings())
    private val matchState = MutableStateFlow(emptyList<Match>())

    val settingsRepository: SettingsRepository = SqlSettingsRepository(db, settingsState)
    val profileRepository: ProfileRepository = SqlProfileRepository(db, settingsState)
    val matchRepository: MatchRepository = SqlMatchRepository(db, profileRepository, matchState)
    val messageRepository: MessageRepository = SqlMessageRepository(db)
    val myProfileRepository: MyProfileRepository = SqlMyProfileRepository(db)

    suspend fun init() {
        profileRepository.seedIfNeeded()
        settingsState.value = (settingsRepository.settings() as MutableStateFlow<Settings>).value
        matchState.value = db.sportSwipeQueries.selectMatches(::matchFromRaw).executeAsList()
    }
}

private fun profileScore(profile: Profile): Int =
    profile.photos.size * 10 + if (profile.verified) 15 else 0 +
        listOf(profile.socialLinks.spotify, profile.socialLinks.instagram, profile.socialLinks.strava).count { it } * 4 +
        profile.activityTags.size * 5

class SqlProfileRepository(
    private val db: SportSwipeDatabase,
    private val settingsFlow: MutableStateFlow<Settings>,
) : ProfileRepository {
    private val likesReceivedState = MutableStateFlow(emptyList<Profile>())

    override suspend fun seedIfNeeded() {
        if (db.sportSwipeQueries.selectMetaByKey("seeded").executeAsOneOrNull() != null) return
        db.transaction {
            FakeDataSeeder.profiles().forEach { p ->
                db.sportSwipeQueries.insertProfile(
                    p.id, p.name, p.age.toLong(), p.distanceKm.toLong(), p.sports.joinToString("|") { it.name },
                    p.objective.name, p.bio, p.photos.joinToString("|"), p.activityTags.joinToString("|") { it.name },
                    if (p.verified) 1L else 0L,
                    listOf(p.socialLinks.spotify, p.socialLinks.instagram, p.socialLinks.strava).joinToString("|") { if (it) "1" else "0" },
                    p.marks.entries.joinToString(";") { "${it.key.name}:${it.value.joinToString(",")}" },
                    profileScore(p).toLong()
                )
            }
            FakeDataSeeder.likesReceivedSeed().forEach { db.sportSwipeQueries.insertLikeReceived(it) }
            val my = FakeDataSeeder.myProfile()
            val args = myProfileToRaw(my)
            db.sportSwipeQueries.insertOrReplaceMyProfile(
                args[0] as Long, args[1] as String, args[2] as Long, args[3] as String,
                args[4] as String, args[5] as String, args[6] as String,
                args[7] as Long, args[8] as Long, args[9] as Long, args[10] as Long, args[11] as Long, args[12] as Long
            )
            db.sportSwipeQueries.insertSettings(1, 0, 5, 10)
            db.sportSwipeQueries.insertMeta("seeded", "1")
        }
        likesReceivedState.value = db.sportSwipeQueries.selectLikesReceived(::profileFromRaw).executeAsList()
    }

    override fun discoverItems(): Flow<List<DiscoverItem>> = settingsFlow.map { settings ->
        db.sportSwipeQueries.selectProfilesByScore(::profileFromRaw).executeAsList().mapIndexed { i, p ->
            if ((i + 1) % settings.adEveryN == 0) {
                DiscoverItem.Ad(AdProfile(-(i + 1).toLong(), "Anuncio", "Potencia tu entrenamiento", "Probar"))
            } else DiscoverItem.User(p)
        }
    }

    override fun likesReceived(): Flow<List<Profile>> = likesReceivedState

    override fun recommendedTop10(): Flow<List<Profile>> = MutableStateFlow(
        db.sportSwipeQueries.selectAnyProfiles(25, ::profileFromRaw).executeAsList()
            .sortedByDescending { (100 - it.distanceKm) + (if (it.verified) 8 else 0) + it.sports.size * 5 }
            .take(10)
    )

    override suspend fun relatedByGroup(group: Objective): List<Profile> =
        db.sportSwipeQueries.selectRelatedByObjective(group.name, ::profileFromRaw).executeAsList()

    override suspend fun like(profileId: Long): Boolean {
        db.sportSwipeQueries.insertLikeGiven(profileId, Clock.System.now().toString())
        return FakeDataSeeder.predefinedMutualLikes().contains(profileId) || profileId % 4L == 0L
    }

    override suspend fun nope(profileId: Long) = Unit
    override suspend fun rewind() = Unit

    override suspend fun getProfile(profileId: Long): Profile? =
        db.sportSwipeQueries.selectProfileById(profileId, ::profileFromRaw).executeAsOneOrNull()
}

class SqlMatchRepository(
    private val db: SportSwipeDatabase,
    private val profileRepository: ProfileRepository,
    private val state: MutableStateFlow<List<Match>>,
) : MatchRepository {
    private val buildPlan = BuildPartnerWorkoutPlanUseCase()

    override fun matches(): Flow<List<Match>> = state

    override suspend fun getMatch(matchId: Long): Match? = state.value.firstOrNull { it.id == matchId }

    override suspend fun createMatch(profileId: Long): Match? {
        val existing = state.value.firstOrNull { it.profileId == profileId }
        if (existing != null) return existing
        val profile = profileRepository.getProfile(profileId) ?: return null
        val plan = buildPlan(null, profile)
        db.sportSwipeQueries.insertMatch(profileId, Clock.System.now().toString(), plan.serialize())
        state.value = db.sportSwipeQueries.selectMatches(::matchFromRaw).executeAsList()
        return state.value.firstOrNull { it.profileId == profileId }
    }
}

class SqlMessageRepository(private val db: SportSwipeDatabase) : MessageRepository {
    override fun messages(matchId: Long): Flow<List<Message>> = MutableStateFlow(
        db.sportSwipeQueries.selectMessagesByMatch(matchId, ::messageFromRaw).executeAsList()
    )

    override suspend fun send(matchId: Long, body: String) {
        db.sportSwipeQueries.insertMessage(matchId, Sender.ME.name, body, Clock.System.now().toString())
    }
}

class SqlSettingsRepository(
    private val db: SportSwipeDatabase,
    private val state: MutableStateFlow<Settings>,
) : SettingsRepository {
    override fun settings(): Flow<Settings> {
        val row = db.sportSwipeQueries.selectSettings().executeAsOneOrNull()
        state.value = row?.let { Settings(it.isPremium == 1L, it.adEveryN.toInt(), it.freeMatchLimit.toInt()) } ?: Settings()
        return state
    }

    override suspend fun setPremium(isPremium: Boolean) = update(state.value.copy(isPremium = isPremium))
    override suspend fun setAdFrequency(n: Int) = update(state.value.copy(adEveryN = n))
    override suspend fun setFreeMatchLimit(limit: Int) = update(state.value.copy(freeMatchLimit = limit))

    private fun update(settings: Settings) {
        val t = settings.serialize()
        db.sportSwipeQueries.insertSettings(1, t.first, t.second, t.third)
        state.value = settings
    }
}

class SqlMyProfileRepository(private val db: SportSwipeDatabase) : MyProfileRepository {
    override fun myProfile(): Flow<MyProfile> = MutableStateFlow(
        db.sportSwipeQueries.selectMyProfile(::myProfileFromRaw).executeAsOneOrNull() ?: FakeDataSeeder.myProfile()
    )

    override suspend fun save(profile: MyProfile) {
        val args = myProfileToRaw(profile)
        db.sportSwipeQueries.insertOrReplaceMyProfile(
            args[0] as Long, args[1] as String, args[2] as Long, args[3] as String,
            args[4] as String, args[5] as String, args[6] as String,
            args[7] as Long, args[8] as Long, args[9] as Long, args[10] as Long, args[11] as Long, args[12] as Long
        )
    }

    override suspend fun deactivate() {
        val current = db.sportSwipeQueries.selectMyProfile(::myProfileFromRaw).executeAsOneOrNull() ?: FakeDataSeeder.myProfile()
        save(current.copy(active = false))
    }

    override suspend fun deleteAllData() {
        db.sportSwipeQueries.deleteAllMessages()
        db.sportSwipeQueries.deleteAllMatches()
        db.sportSwipeQueries.deleteAllLikesGiven()
        db.sportSwipeQueries.deleteAllProfiles()
        db.sportSwipeQueries.deleteLikesReceived()
        db.sportSwipeQueries.deleteMetaByKey("seeded")
    }
}
