package com.sportswipe.shared.domain.model

import kotlinx.datetime.Instant

enum class Objective { GYMBRO, COACH, SPOTTER, TRAIN_TODAY, WEEKEND_TRAINING }
enum class ActivityTag { ONLINE, RECENTLY_ACTIVE, NEW }
enum class Sport { GYM, RUNNING, CYCLING, CROSSFIT, YOGA, SWIMMING, TENNIS }

data class SocialLinks(
    val spotify: Boolean,
    val instagram: Boolean,
    val strava: Boolean,
)

data class Profile(
    val id: Long,
    val name: String,
    val age: Int,
    val distanceKm: Int,
    val sports: List<Sport>,
    val objective: Objective,
    val bio: String,
    val photos: List<String>,
    val activityTags: List<ActivityTag>,
    val verified: Boolean,
    val socialLinks: SocialLinks,
    val marks: Map<Sport, List<String>>,
)

data class AdProfile(
    val id: Long,
    val title: String,
    val description: String,
    val cta: String,
)

sealed interface DiscoverItem {
    data class User(val profile: Profile) : DiscoverItem
    data class Ad(val ad: AdProfile) : DiscoverItem
}

data class Match(
    val id: Long,
    val profileId: Long,
    val createdAt: Instant,
    val workoutPlan: PartnerWorkoutPlan,
)

data class Message(
    val id: Long,
    val matchId: Long,
    val sender: Sender,
    val body: String,
    val sentAt: Instant,
)

enum class Sender { ME, THEM }

data class PartnerWorkoutPlan(
    val warmup: String,
    val exercises: List<String>,
    val scaling: String,
    val durationMinutes: Int,
    val safetyDisclaimer: String,
)

data class MyProfile(
    val id: Long = 1,
    val name: String,
    val age: Int,
    val bio: String,
    val sports: List<Sport>,
    val goals: List<Objective>,
    val marks: Map<Sport, List<String>>,
    val showGym: Boolean,
    val showZone: Boolean,
    val linkedSpotify: Boolean,
    val linkedInstagram: Boolean,
    val linkedStrava: Boolean,
    val active: Boolean = true,
)

data class Settings(
    val isPremium: Boolean = false,
    val adEveryN: Int = 5,
    val freeMatchLimit: Int = 10,
)
