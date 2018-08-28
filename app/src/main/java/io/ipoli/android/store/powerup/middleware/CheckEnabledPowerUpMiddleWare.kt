package io.ipoli.android.store.powerup.middleware

import io.ipoli.android.Constants
import io.ipoli.android.challenge.add.EditChallengeAction
import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.common.AppState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.Dispatcher
import io.ipoli.android.common.redux.MiddleWare
import io.ipoli.android.common.view.DurationPickerDialogAction
import io.ipoli.android.growth.GrowthAction
import io.ipoli.android.habit.list.HabitListAction
import io.ipoli.android.player.data.Inventory
import io.ipoli.android.store.powerup.PowerUp
import io.ipoli.android.tag.list.TagListAction

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/20/2018.
 */
data class ShowBuyPowerUpAction(val powerUp: PowerUp.Type) : Action

object CheckEnabledPowerUpMiddleWare : MiddleWare<AppState> {

    override fun execute(
        state: AppState,
        dispatcher: Dispatcher,
        action: Action
    ): MiddleWare.Result {
        val p = state.dataState.player ?: return MiddleWare.Result.Continue

        val inventory = p.inventory

        return when (action) {

            is GrowthAction.ShowWeek,
            is GrowthAction.ShowMonth ->
                checkForAvailablePowerUp(PowerUp.Type.GROWTH, inventory, dispatcher)

//            is ChallengeListAction.AddChallenge ->
//                checkForAvailablePowerUp(PowerUp.Type.CHALLENGES, inventory, dispatcher)

//            is QuestAction.Start ->
//                checkForAvailablePowerUp(PowerUp.Type.TIMER, inventory, dispatcher)

            is HabitListAction.Add -> {
                val habits = state.dataState.habits
                if (habits == null || habits.size < Constants.MAX_FREE_HABITS) {
                    MiddleWare.Result.Continue
                } else {
                    checkForAvailablePowerUp(PowerUp.Type.HABITS, inventory, dispatcher)
                }
            }

            is TagListAction.AddTag ->
                if (state.dataState.tags.size < Constants.MAX_FREE_TAGS)
                    MiddleWare.Result.Continue
                else
                    checkForAvailablePowerUp(PowerUp.Type.TAGS, inventory, dispatcher)

            is DurationPickerDialogAction.ShowCustom ->
                checkForAvailablePowerUp(PowerUp.Type.CUSTOM_DURATION, inventory, dispatcher)

//            is QuestAction.AddSubQuest,
//            is EditRepeatingQuestAction.AddSubQuest ->
//                checkForAvailablePowerUp(PowerUp.Type.SUB_QUESTS, inventory, dispatcher)
//
//            is NoteAction.Save ->
//                checkForAvailablePowerUp(PowerUp.Type.NOTES, inventory, dispatcher)
//
//            is SettingsAction.ToggleSyncCalendar ->
//                checkForAvailablePowerUp(PowerUp.Type.CALENDAR_SYNC, inventory, dispatcher)

            is EditChallengeAction.ShowTargetTrackedValuePicker -> {
                checkForAddChallengeValue(action.trackedValues, inventory, dispatcher)
            }

            is EditChallengeAction.ShowAverageTrackedValuePicker -> {
                checkForAddChallengeValue(action.trackedValues, inventory, dispatcher)
            }

            else -> MiddleWare.Result.Continue
        }
    }

    private fun checkForAddChallengeValue(
        trackedValues: List<Challenge.TrackedValue>,
        inventory: Inventory,
        dispatcher: Dispatcher
    ) =
        if (trackedValues.any { it !is Challenge.TrackedValue.Progress }) {
            checkForAvailablePowerUp(
                PowerUp.Type.TRACK_CHALLENGE_VALUES,
                inventory,
                dispatcher
            )
        } else {
            MiddleWare.Result.Continue
        }


    private fun checkForAvailablePowerUp(
        powerUp: PowerUp.Type,
        inventory: Inventory,
        dispatcher: Dispatcher
    ) =
        when {
            inventory.isPowerUpEnabled(powerUp) -> MiddleWare.Result.Continue
            else -> {
                dispatcher.dispatch(ShowBuyPowerUpAction(powerUp))
                MiddleWare.Result.Stop
            }
        }

}