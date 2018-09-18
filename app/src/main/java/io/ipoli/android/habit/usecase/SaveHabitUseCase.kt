package io.ipoli.android.habit.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.habit.data.Habit
import io.ipoli.android.habit.persistence.HabitRepository
import io.ipoli.android.player.data.Player
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.player.usecase.RemoveRewardFromPlayerUseCase
import io.ipoli.android.player.usecase.RewardPlayerUseCase
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import io.ipoli.android.tag.Tag
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDateTime

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 6/17/18.
 */
class SaveHabitUseCase(
    private val habitRepository: HabitRepository,
    private val playerRepository: PlayerRepository,
    private val rewardPlayerUseCase: RewardPlayerUseCase,
    private val removeRewardFromPlayerUseCase: RemoveRewardFromPlayerUseCase
) : UseCase<SaveHabitUseCase.Params, Habit> {

    override fun execute(parameters: Params): Habit {
        val habit = if (parameters.id.isBlank()) {
            Habit(
                name = parameters.name,
                color = parameters.color,
                icon = parameters.icon,
                tags = parameters.tags ?: emptyList(),
                days = parameters.days,
                timesADay = parameters.timesADay,
                isGood = parameters.isGood,
                challengeId = parameters.challengeId,
                note = parameters.note
            )
        } else {
            var h = habitRepository.findById(parameters.id)!!

            if (h.timesADay != parameters.timesADay) {
                h = handleRewardIfTimesADayUpdated(
                    h,
                    parameters.timesADay,
                    parameters.dateTime,
                    parameters.player
                )
            }

            h.copy(
                name = parameters.name,
                color = parameters.color,
                icon = parameters.icon,
                tags = parameters.tags ?: emptyList(),
                days = parameters.days,
                timesADay = parameters.timesADay,
                isGood = parameters.isGood,
                challengeId = parameters.challengeId,
                note = parameters.note
            )
        }

        return habitRepository.save(habit)
    }

    private fun handleRewardIfTimesADayUpdated(
        habit: Habit,
        timesADay: Int,
        dateTime: LocalDateTime,
        player: Player?
    ): Habit {
        val p = player ?: playerRepository.find()!!
        val date = p.currentDate(dateTime)
        val resetTime = p.preferences.resetDayTime

        if (!habit.shouldBeDoneOn(dateTime, resetTime)) {
            return habit
        }

        val completedCountForDate = habit.completedCountForDate(dateTime, resetTime)

        if (completedCountForDate >= timesADay) {

            val reward =
                rewardPlayerUseCase.execute(RewardPlayerUseCase.Params.ForHabit(habit, date, p))
                    .reward

            val history = habit.history.toMutableMap()

            history[date] = history[date]!!.copy(reward = reward)

            return habit.copy(
                history = history
            )
        }

        if (habit.isCompletedFor(dateTime, resetTime)) {
            val history = habit.history
            removeRewardFromPlayerUseCase.execute(RemoveRewardFromPlayerUseCase.Params(history[date]!!.reward!!))
        }

        return habit
    }

    data class Params(
        val id: String = "",
        val name: String,
        val color: Color,
        val icon: Icon,
        val tags: List<Tag>? = null,
        val days: Set<DayOfWeek>,
        val timesADay: Int,
        val isGood: Boolean,
        val challengeId: String? = null,
        val note: String = "",
        val player: Player? = null,
        val dateTime: LocalDateTime = LocalDateTime.now()
    )
}