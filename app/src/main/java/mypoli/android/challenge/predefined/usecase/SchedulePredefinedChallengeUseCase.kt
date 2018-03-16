package mypoli.android.challenge.predefined.usecase

import mypoli.android.challenge.predefined.entity.PredefinedChallengeData
import mypoli.android.common.UseCase
import mypoli.android.common.datetime.datesBetween
import mypoli.android.quest.BaseQuest
import mypoli.android.quest.Category
import mypoli.android.quest.Quest
import mypoli.android.quest.RepeatingQuest
import mypoli.android.repeatingquest.entity.RepeatingPattern
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.TemporalAdjusters
import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */
class SchedulePredefinedChallengeUseCase :
    UseCase<SchedulePredefinedChallengeUseCase.Params, List<BaseQuest>> {

    private lateinit var startDate: LocalDate
    private lateinit var endDate: LocalDate

    override fun execute(parameters: Params): List<BaseQuest> {
        val challenge = parameters.challenge

        require(challenge.quests.isNotEmpty(), { "Challenge must contain quests" })

        startDate = parameters.startDate
        endDate = startDate.plusDays((challenge.durationDays - 1).toLong())
        val randomSeed = parameters.randomSeed

        return challenge.quests.map { q ->
            when (q) {
                is PredefinedChallengeData.Quest.Repeating -> {
                    val rq = RepeatingQuest(
                        name = q.name,
                        color = q.color,
                        icon = q.icon,
                        startTime = q.startTime,
                        category = Category(challenge.category.name, q.color),
                        duration = q.duration,
                        repeatingPattern = if (q.weekDays.isEmpty()) {
                            RepeatingPattern.Daily(startDate, endDate)
                        } else {
                            RepeatingPattern.Weekly(
                                daysOfWeek = q.weekDays.toSet(),
                                start = startDate,
                                end = endDate
                            )
                        }
                    )
                    listOf<BaseQuest>(rq)
                }

                is PredefinedChallengeData.Quest.OneTime -> {

                    val scheduledDate = if (q.startAtDay != null) {
                        val startDay = startDate.plusDays((q.startAtDay - 1).toLong())
                        if (startDay.isAfter(endDate)) {
                            chooseRandomScheduledDate(randomSeed)
                        } else {
                            startDay
                        }
                    } else if (q.preferredDayOfWeek != null) {
                        val preferredDate =
                            startDate.with(TemporalAdjusters.nextOrSame(q.preferredDayOfWeek))
                        if (preferredDate.isAfter(endDate)) {
                            chooseRandomScheduledDate(randomSeed)
                        } else {
                            preferredDate
                        }
                    } else {
                        chooseRandomScheduledDate(randomSeed)
                    }

                    listOf<BaseQuest>(createFromOneTime(q, challenge, scheduledDate))
                }
            }
        }.flatten()
    }

    private fun chooseRandomScheduledDate(randomSeed: Long?): LocalDate {
        val dates = startDate.datesBetween(endDate)

        val random = randomSeed?.let {
            Random(it)
        } ?: Random()

        return dates[random.nextInt(dates.size)]
    }

    private fun createFromOneTime(
        it: PredefinedChallengeData.Quest.OneTime,
        challenge: PredefinedChallengeData,
        scheduledDate: LocalDate
    ) =
        Quest(
            name = it.name,
            color = it.color,
            icon = it.icon,
            startTime = it.startTime,
            category = Category(challenge.category.name, it.color),
            duration = it.duration,
            scheduledDate = scheduledDate
        )

    data class Params(
        val challenge: PredefinedChallengeData,
        val startDate: LocalDate = LocalDate.now(),
        val randomSeed: Long? = null
    )
}