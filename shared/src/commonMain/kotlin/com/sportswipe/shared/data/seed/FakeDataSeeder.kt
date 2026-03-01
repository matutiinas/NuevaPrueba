package com.sportswipe.shared.data.seed

import com.sportswipe.shared.domain.model.ActivityTag
import com.sportswipe.shared.domain.model.MyProfile
import com.sportswipe.shared.domain.model.Objective
import com.sportswipe.shared.domain.model.Profile
import com.sportswipe.shared.domain.model.SocialLinks
import com.sportswipe.shared.domain.model.Sport

object FakeDataSeeder {
    private val names = listOf("Alex", "Sam", "Cris", "Nico", "Luna", "Mia", "Dani", "Leo", "Noa", "Kai")
    private val bios = listOf(
        "Buscando partner para romper marcas.",
        "Entreno funcional y buena vibra.",
        "Cardio, fuerza y constancia.",
        "Mejor en equipo 💪",
        "Gym temprano, running tarde."
    )

    fun myProfile() = MyProfile(
        name = "You",
        age = 27,
        bio = "Siempre listo para entrenar.",
        sports = listOf(Sport.GYM, Sport.RUNNING),
        goals = listOf(Objective.GYMBRO, Objective.TRAIN_TODAY),
        marks = mapOf(Sport.GYM to listOf("Press banca: 90kg"), Sport.RUNNING to listOf("5K: 24:10")),
        showGym = true,
        showZone = true,
        linkedSpotify = true,
        linkedInstagram = true,
        linkedStrava = false,
    )

    fun profiles(): List<Profile> = (1L..50L).map { id ->
        val sportA = Sport.entries[(id % Sport.entries.size).toInt()]
        val sportB = Sport.entries[((id + 2) % Sport.entries.size).toInt()]
        val objective = Objective.entries[(id % Objective.entries.size).toInt()]
        Profile(
            id = id,
            name = "${names[(id % names.size).toInt()]} $id",
            age = (20 + (id % 13)).toInt(),
            distanceKm = (1 + (id % 30)).toInt(),
            sports = listOf(sportA, sportB).distinct(),
            objective = objective,
            bio = bios[(id % bios.size).toInt()],
            photos = listOf("avatar_${id % 15}", "avatar_${(id + 3) % 15}"),
            activityTags = listOf(ActivityTag.ONLINE, if (id % 2L == 0L) ActivityTag.RECENTLY_ACTIVE else ActivityTag.NEW),
            verified = id % 3L == 0L,
            socialLinks = SocialLinks(spotify = id % 2L == 0L, instagram = true, strava = id % 4L == 0L),
            marks = mapOf(
                sportA to listOf("Marca ${(id % 10) + 1}"),
                sportB to listOf("Tiempo ${(20 + id % 20)} min")
            )
        )
    }

    fun likesReceivedSeed(): List<Long> = listOf(2, 4, 8, 12, 16, 24, 30, 35, 40, 48)

    fun predefinedMutualLikes(): Set<Long> = setOf(3, 7, 10, 14, 18, 21, 28, 33, 41, 47)
}
