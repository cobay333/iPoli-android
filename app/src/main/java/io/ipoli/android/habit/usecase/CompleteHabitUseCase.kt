package io.ipoli.android.habit.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.habit.data.CompletedEntry
import io.ipoli.android.habit.data.Habit
import io.ipoli.android.habit.persistence.HabitRepository
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.player.usecase.RewardPlayerUseCase
import io.ipoli.android.quest.job.RewardScheduler
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 6/17/18.
 */
class CompleteHabitUseCase(
    private val habitRepository: HabitRepository,
    private val playerRepository: PlayerRepository,
    private val rewardPlayerUseCase: RewardPlayerUseCase,
    private val rewardScheduler: RewardScheduler
) : UseCase<CompleteHabitUseCase.Params, Habit> {

    override fun execute(parameters: Params): Habit {
        val habit = habitRepository.findById(parameters.habitId)
        requireNotNull(habit)

        val player = playerRepository.find()!!

        val date = parameters.date

        require(!habit!!.isCompletedForDate(date))

        val history = habit.history.toMutableMap()

        val completedEntry =
            if (history.containsKey(date)) history[date]!!
            else CompletedEntry()

        history[date] = completedEntry.complete()

        val isCompleted = habit.copy(
            history = history
        ).isCompletedForDate(date)

        if (isCompleted) {
            history[date] = history[date] ?: CompletedEntry()
        }

        return if (isCompleted) {
            if (habit.isGood) {
                val reward = rewardPlayerUseCase.execute(
                    RewardPlayerUseCase.Params.ForHabit(
                        habit.copy(history = history),
                        playerDate = date,
                        player = player
                    )
                ).reward
                history[date] = history[date]!!.copy(
                    reward = reward
                )
                val h = saveHabit(habit, history)
                rewardScheduler.schedule(
                    reward = reward,
                    type = RewardScheduler.Type.HABIT,
                    entityId = habit.id
                )
                h

            } else {
                val reward = rewardPlayerUseCase.execute(
                    RewardPlayerUseCase.Params.ForBadHabit(
                        habit.copy(history = history),
                        playerDate = date,
                        player = player
                    )
                ).reward
                history[date] = history[date]!!.copy(
                    reward = reward
                )
                val h = saveHabit(habit, history)
                rewardScheduler.schedule(
                    reward = reward.copy(
                        experience = -reward.experience,
                        coins = -reward.coins,
                        attributePoints = reward.attributePoints.map {
                            it.key to -it.value
                        }.toMap()
                    ),
                    isPositive = false,
                    type = RewardScheduler.Type.HABIT,
                    entityId = habit.id
                )
                h
            }
        } else {
            saveHabit(habit, history)
        }
    }

    private fun saveHabit(
        habit: Habit,
        history: MutableMap<LocalDate, CompletedEntry>
    ) =
        habitRepository.save(habit.copy(history = history))

    data class Params(val habitId: String, val date: LocalDate = LocalDate.now())
}