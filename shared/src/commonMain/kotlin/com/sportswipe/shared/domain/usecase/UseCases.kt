package com.sportswipe.shared.domain.usecase

import com.sportswipe.shared.domain.model.Match
import com.sportswipe.shared.domain.model.Objective
import com.sportswipe.shared.domain.model.PartnerWorkoutPlan
import com.sportswipe.shared.domain.model.Profile
import com.sportswipe.shared.domain.model.Settings
import com.sportswipe.shared.domain.model.Sport
import com.sportswipe.shared.domain.repository.MatchRepository
import com.sportswipe.shared.domain.repository.MyProfileRepository
import com.sportswipe.shared.domain.repository.ProfileRepository
import com.sportswipe.shared.domain.repository.SettingsRepository
import kotlin.math.max
import kotlinx.coroutines.flow.Flow

class SeedAppDataUseCase(private val profileRepository: ProfileRepository) {
    suspend operator fun invoke() = profileRepository.seedIfNeeded()
}

class LikeProfileUseCase(
    private val profileRepository: ProfileRepository,
    private val matchRepository: MatchRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(profileId: Long, currentMatches: Int, settings: Settings): Match? {
        val overLimit = !settings.isPremium && currentMatches >= settings.freeMatchLimit
        if (overLimit) return null
        val isMutual = profileRepository.like(profileId)
        return if (isMutual) matchRepository.createMatch(profileId) else null
    }
}

class BuildPartnerWorkoutPlanUseCase {
    operator fun invoke(me: Profile?, other: Profile): PartnerWorkoutPlan {
        val sharedSports = me?.sports?.intersect(other.sports.toSet())?.toList().orEmpty()
        val mainSport = sharedSports.firstOrNull() ?: Sport.GYM
        val base = when (mainSport) {
            Sport.RUNNING -> listOf("Warm jog 10 min", "Intervals 8x200m", "Cooldown walk")
            Sport.CYCLING -> listOf("Easy ride 8 min", "Partner cadence drills", "Hill repeats")
            Sport.CROSSFIT -> listOf("Joint mobility", "Partner AMRAP 16'", "Core finisher")
            else -> listOf("Movilidad dinámica", "Sentadilla sincronizada", "Push + pull en parejas", "Core + farmer carry")
        }
        val objectiveExercise = when (other.objective) {
            Objective.COACH -> "Técnica guiada por bloques de 5 min"
            Objective.SPOTTER -> "Bloques de fuerza con spotter"
            Objective.TRAIN_TODAY -> "Circuito express HIIT"
            Objective.WEEKEND_TRAINING -> "Sesión larga funcional"
            Objective.GYMBRO -> "Superseries de hipertrofia"
        }
        return PartnerWorkoutPlan(
            warmup = "8-10 min de activación y movilidad articular",
            exercises = (base + objectiveExercise).take(6),
            scaling = "Escalar carga entre 50-80% del 1RM estimado y reducir repeticiones si falla técnica.",
            durationMinutes = max(35, 20 + base.size * 7),
            safetyDisclaimer = "Mantén técnica correcta, hidrátate y detén el entrenamiento ante dolor agudo.",
        )
    }
}

class GetRelatedGroupProfilesUseCase(private val profileRepository: ProfileRepository) {
    suspend operator fun invoke(objective: Objective): List<Profile> = profileRepository.relatedByGroup(objective)
}

class ObservePremiumUseCase(private val settingsRepository: SettingsRepository) {
    operator fun invoke(): Flow<Settings> = settingsRepository.settings()
}

class DeleteAccountUseCase(private val myProfileRepository: MyProfileRepository) {
    suspend operator fun invoke() = myProfileRepository.deleteAllData()
}
