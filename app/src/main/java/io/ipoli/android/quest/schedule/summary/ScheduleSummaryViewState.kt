package io.ipoli.android.quest.schedule.summary

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.quest.schedule.summary.ScheduleSummaryViewState.StateType.*
import io.ipoli.android.quest.schedule.summary.usecase.CreateScheduleSummaryItemsUseCase
import io.ipoli.android.quest.usecase.Schedule
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 07/03/2018.
 */
sealed class ScheduleSummaryAction : Action {

    object Load : ScheduleSummaryAction()
    data class ChangeDate(val date: LocalDate) : ScheduleSummaryAction()
    data class ChangeMonth(val yearMonth: YearMonth) : ScheduleSummaryAction()

    object GoToToday : ScheduleSummaryAction()
}

object ScheduleSummaryReducer : BaseViewStateReducer<ScheduleSummaryViewState>() {

    override val stateKey = key<ScheduleSummaryViewState>()

    override fun reduce(
        state: AppState,
        subState: ScheduleSummaryViewState,
        action: Action
    ) =
        when (action) {

            is ScheduleSummaryAction.Load ->
                subState.copy(
                    type = DATE_DATA_CHANGED,
                    currentDate = state.dataState.agendaDate
                )

            is ScheduleSummaryAction.ChangeDate ->
                subState.copy(
                    type = DATE_SELECTED,
                    currentDate = action.date
                )

            is ScheduleSummaryAction.ChangeMonth ->
                subState.copy(
                    type = MONTH_CHANGED,
                    currentDate = LocalDate.of(
                        action.yearMonth.year,
                        action.yearMonth.month,
                        1
                    )
                )

            is DataLoadedAction.ScheduleSummaryChanged -> {
                if (subState.currentDate == action.currentDate) {
                    subState.copy(
                        type = SCHEDULE_SUMMARY_DATA_CHANGED,
                        items = action.schedules
                    )
                } else subState
            }

            else -> subState
        }

    override fun defaultState() =
        ScheduleSummaryViewState(
            type = LOADING,
            currentDate = LocalDate.now(),
            items = emptyList(),
            schedule = null
        )
}

data class ScheduleSummaryViewState(
    val type: StateType,
    val currentDate: LocalDate,
    val items: List<CreateScheduleSummaryItemsUseCase.Schedule>,
    val schedule: Schedule?
) : BaseViewState() {
    enum class StateType {
        LOADING,
        DATE_DATA_CHANGED,
        MONTH_CHANGED,
        SCHEDULE_SUMMARY_DATA_CHANGED,
        DATE_SELECTED
    }
}