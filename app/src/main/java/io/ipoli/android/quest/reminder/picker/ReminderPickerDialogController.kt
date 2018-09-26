package io.ipoli.android.quest.reminder.picker

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.view.ReduxDialogController
import io.ipoli.android.common.view.stringRes
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.quest.reminder.formatter.ReminderTimeFormatter
import io.ipoli.android.quest.reminder.formatter.TimeUnitFormatter
import kotlinx.android.synthetic.main.dialog_reminder_picker.view.*
import kotlinx.android.synthetic.main.view_dialog_header.view.*

typealias TimeUnitConverter = java.util.concurrent.TimeUnit

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/4/17.
 */

enum class TimeUnit(val minutes: Long) {

    MINUTES(1),
    HOURS(60),
    DAYS(TimeUnitConverter.DAYS.toMinutes(1)),
    WEEKS(TimeUnitConverter.DAYS.toMinutes(7))
}

data class ReminderViewModel(val message: String, val minutesFromStart: Long)

class ReminderPickerDialogController :
    ReduxDialogController<ReminderPickerAction, ReminderPickerViewState, ReminderPickerReducer> {

    private var listener: ReminderPickedListener? = null

    private var reminder: ReminderViewModel? = null

    override val reducer = ReminderPickerReducer

    constructor(
        listener: ReminderPickedListener,
        selectedReminder: ReminderViewModel? = null
    ) : this() {
        this.listener = listener
        this.reminder = selectedReminder
    }

    constructor(args: Bundle? = null) : super(args)

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.setText(R.string.reminder_dialog_title)
    }

    @SuppressLint("InflateParams")
    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        val contentView = inflater.inflate(R.layout.dialog_reminder_picker, null)

        with(contentView) {

            message.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    dispatch(ReminderPickerAction.ChangeMessage(s.toString()))
                }

            })

            predefinedTimes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    dispatch(ReminderPickerAction.ChangePredefinedTime(position))
                }
            }

            customTimeUnits.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    dispatch(ReminderPickerAction.ChangeTimeUnit(position))
                }

            }

            customTime.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    dispatch(ReminderPickerAction.ChangeCustomTime(s.toString()))
                }

            })
        }
        return contentView
    }

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog =
        dialogBuilder
            .setPositiveButton(R.string.dialog_ok, null)
            .setNegativeButton(R.string.cancel, { _, _ -> ViewUtils.hideKeyboard(contentView) })
            .setNeutralButton(R.string.do_not_remind, { _, _ ->
                listener?.onReminderPicked(null)
            })
            .create()

    override fun onDialogCreated(dialog: AlertDialog, contentView: View) {
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).onDebounceClick {
                dispatch(ReminderPickerAction.PickReminder)
            }
        }
    }

    override fun onCreateLoadAction() = ReminderPickerAction.Load(reminder)

    override fun render(state: ReminderPickerViewState, view: View) {

        when (state.type) {

            ReminderPickerViewState.StateType.NEW_REMINDER -> {
                changeIcon(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)
                ViewUtils.showViews(view.predefinedTimes)
                ViewUtils.goneViews(view.customTimeContainer)
                view.message.setText(state.message)
                view.message.setSelection(state.message.length)

                val predefinedTimesAdapter = ArrayAdapter(
                    activity!!,
                    android.R.layout.simple_spinner_item,
                    state.predefinedValues
                )
                predefinedTimesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                view.predefinedTimes.adapter = predefinedTimesAdapter
                view.predefinedTimes.setSelection(state.predefinedIndex!!)
            }

            ReminderPickerViewState.StateType.EDIT_REMINDER -> {
                changeIcon(AndroidPetAvatar.valueOf(state.petAvatar!!.name).headImage)
                view.message.setText(state.message)
                view.message.setSelection(state.message.length)

                if (state.predefinedIndex != null) {
                    ViewUtils.showViews(view.predefinedTimes)
                    ViewUtils.goneViews(view.customTimeContainer)
                    val predefinedTimesAdapter = ArrayAdapter(
                        activity!!,
                        android.R.layout.simple_spinner_item,
                        state.predefinedValues
                    )
                    predefinedTimesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    view.predefinedTimes.adapter = predefinedTimesAdapter
                    view.predefinedTimes.setSelection(state.predefinedIndex)
                }

                if (state.timeUnitIndex != null) {

                    showCustomTimeViews(view)

                    val customTimeAdapter = ArrayAdapter(
                        activity!!,
                        android.R.layout.simple_spinner_item,
                        state.timeUnits
                    )
                    customTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    view.customTimeUnits.adapter = customTimeAdapter
                    view.customTimeUnits.setSelection(state.timeUnitIndex)
                    view.customTime.setText(state.timeValue)
                }
            }

            ReminderPickerViewState.StateType.CUSTOM_TIME -> {
                showCustomTimeViews(view)

                val customTimeAdapter =
                    ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, state.timeUnits)
                customTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                view.customTimeUnits.adapter = customTimeAdapter
                view.customTimeUnits.setSelection(state.timeUnitIndex!!)
                view.customTime.setText(state.timeValue)

                view.customTime.requestFocus()
                ViewUtils.showKeyboard(activity!!, view.customTime)
            }

            ReminderPickerViewState.StateType.FINISHED -> {
                listener?.onReminderPicked(state.viewModel!!)
                dismiss()
            }

            ReminderPickerViewState.StateType.TIME_VALUE_VALIDATION_ERROR -> {
                view.customTime.error = stringRes(R.string.invalid_reminder_time)
            }

            else -> {
            }
        }
    }

    private fun showCustomTimeViews(dialogView: View) {
        ViewUtils.showViews(dialogView.customTimeContainer)
        ViewUtils.goneViews(dialogView.predefinedTimes)
    }

    interface ReminderPickedListener {
        fun onReminderPicked(reminder: ReminderViewModel?)
    }

    private val ReminderPickerViewState.timeUnits
        get() = TimeUnitFormatter(activity!!).customTimeUnits

    private val ReminderPickerViewState.predefinedValues: List<String>
        get() = ReminderTimeFormatter.predefinedTimes(activity!!)
}