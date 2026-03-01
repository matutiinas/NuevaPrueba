package com.sportswipe.shared.data.mapper

import com.sportswipe.shared.domain.model.ActivityTag
import com.sportswipe.shared.domain.model.Match
import com.sportswipe.shared.domain.model.Message
import com.sportswipe.shared.domain.model.MyProfile
import com.sportswipe.shared.domain.model.Objective
import com.sportswipe.shared.domain.model.PartnerWorkoutPlan
import com.sportswipe.shared.domain.model.Profile
import com.sportswipe.shared.domain.model.Sender
import com.sportswipe.shared.domain.model.Settings
import com.sportswipe.shared.domain.model.SocialLinks
import com.sportswipe.shared.domain.model.Sport
import kotlinx.datetime.Instant

private fun <T : Enum<T>> parseEnums(raw: String, values: Array<T>): List<T> =
    raw.split("|").filter { it.isNotBlank() }.mapNotNull { token -> values.firstOrNull { it.name == token } }

private fun serializeEnums(values: List<Enum<*>>) = values.joinToString("|") { it.name }

fun Profile.toRow(score: Int): ProfileRow = ProfileRow(
    id, name, age.toLong(), distanceKm.toLong(), serializeEnums(sports), objective.name, bio,
    photos.joinToString("|"), serializeEnums(activityTags), if (verified) 1 else 0,
    listOf(socialLinks.spotify, socialLinks.instagram, socialLinks.strava).joinToString("|") { if (it) "1" else "0" },
    marks.entries.joinToString(";") { "${it.key.name}:${it.value.joinToString(",")}" },
    score.toLong()
)

data class ProfileRow(
    val id: Long,
    val name: String,
    val age: Long,
    val distanceKm: Long,
    val sports: String,
    val objective: String,
    val bio: String,
    val photos: String,
    val activityTags: String,
    val verified: Long,
    val socialLinks: String,
    val marks: String,
    val score: Long,
)

fun profileFromRaw(
    id: Long,
    name: String,
    age: Long,
    distanceKm: Long,
    sports: String,
    objective: String,
    bio: String,
    photos: String,
    activityTags: String,
    verified: Long,
    socialLinks: String,
    marks: String,
): Profile {
    val links = socialLinks.split("|")
    return Profile(
        id = id,
        name = name,
        age = age.toInt(),
        distanceKm = distanceKm.toInt(),
        sports = parseEnums(sports, enumValues<Sport>()),
        objective = Objective.valueOf(objective),
        bio = bio,
        photos = photos.split("|").filter { it.isNotBlank() },
        activityTags = parseEnums(activityTags, enumValues<ActivityTag>()),
        verified = verified == 1L,
        socialLinks = SocialLinks(links.getOrNull(0) == "1", links.getOrNull(1) == "1", links.getOrNull(2) == "1"),
        marks = marks.split(";").filter { it.contains(":") }.associate { pair ->
            val (sport, values) = pair.split(":", limit = 2)
            Sport.valueOf(sport) to values.split(",").filter { it.isNotBlank() }
        },
    )
}

fun Settings.serialize(): Triple<Long, Long, Long> = Triple(if (isPremium) 1, adEveryN.toLong(), freeMatchLimit.toLong())

fun myProfileToRaw(myProfile: MyProfile): List<Any> = listOf(
    myProfile.id,
    myProfile.name,
    myProfile.age.toLong(),
    myProfile.bio,
    serializeEnums(myProfile.sports),
    serializeEnums(myProfile.goals),
    myProfile.marks.entries.joinToString(";") { "${it.key.name}:${it.value.joinToString(",")}" },
    if (myProfile.showGym) 1L else 0L,
    if (myProfile.showZone) 1L else 0L,
    if (myProfile.linkedSpotify) 1L else 0L,
    if (myProfile.linkedInstagram) 1L else 0L,
    if (myProfile.linkedStrava) 1L else 0L,
    if (myProfile.active) 1L else 0L,
)

fun myProfileFromRaw(
    id: Long,
    name: String,
    age: Long,
    bio: String,
    sports: String,
    goals: String,
    marks: String,
    showGym: Long,
    showZone: Long,
    linkedSpotify: Long,
    linkedInstagram: Long,
    linkedStrava: Long,
    active: Long,
): MyProfile = MyProfile(
    id = id,
    name = name,
    age = age.toInt(),
    bio = bio,
    sports = parseEnums(sports, enumValues<Sport>()),
    goals = parseEnums(goals, enumValues<Objective>()),
    marks = marks.split(";").filter { it.contains(":") }.associate {
        val (sport, values) = it.split(":", limit = 2)
        Sport.valueOf(sport) to values.split(",").filter { s -> s.isNotBlank() }
    },
    showGym = showGym == 1L,
    showZone = showZone == 1L,
    linkedSpotify = linkedSpotify == 1L,
    linkedInstagram = linkedInstagram == 1L,
    linkedStrava = linkedStrava == 1L,
    active = active == 1L,
)

fun matchFromRaw(id: Long, profileId: Long, createdAt: String, workoutPlan: String): Match {
    val parts = workoutPlan.split("###")
    return Match(
        id = id,
        profileId = profileId,
        createdAt = Instant.parse(createdAt),
        workoutPlan = PartnerWorkoutPlan(
            warmup = parts.getOrElse(0) { "Warmup" },
            exercises = parts.getOrElse(1) { "Squat|Push-up" }.split("|"),
            scaling = parts.getOrElse(2) { "Scale" },
            durationMinutes = parts.getOrElse(3) { "40" }.toInt(),
            safetyDisclaimer = parts.getOrElse(4) { "Safety" },
        ),
    )
}

fun PartnerWorkoutPlan.serialize(): String = listOf(
    warmup,
    exercises.joinToString("|"),
    scaling,
    durationMinutes.toString(),
    safetyDisclaimer,
).joinToString("###")

fun messageFromRaw(id: Long, matchId: Long, sender: String, body: String, sentAt: String): Message = Message(
    id = id,
    matchId = matchId,
    sender = Sender.valueOf(sender),
    body = body,
    sentAt = Instant.parse(sentAt),
)
