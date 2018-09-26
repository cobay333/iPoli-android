package io.ipoli.android.quest.schedule.addquest

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.annotation.ColorInt
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.minutes
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.edit.EditQuestViewController
import io.ipoli.android.quest.schedule.addquest.StateType.*
import kotlinx.android.synthetic.main.controller_add_quest.view.*
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 11/2/17.
 */
class AddQuestViewController(args: Bundle? = null) :
    ReduxViewController<AddQuestAction, AddQuestViewState, AddQuestReducer>(
        args
    ) {
    override val reducer = AddQuestReducer

    private var closeListener: () -> Unit = {}

    private var isFullscreen: Boolean = false

    private var currentDate: LocalDate? = null

    constructor(
        closeListener: () -> Unit,
        currentDate: LocalDate?,
        isFullscreen: Boolean = false
    ) : this() {
        this.closeListener = closeListener
        this.currentDate = currentDate
        this.isFullscreen = isFullscreen
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.controller_add_quest, container, false)
        view.questName.setOnEditTextImeBackListener(object : EditTextImeBackListener {
            override fun onImeBack(ctrl: EditTextBackEvent, text: String) {
                resetForm(view)
                closeListener()
                view.questName.setOnEditTextImeBackListener(null)
            }
        })

        if (isFullscreen) {
            view.onDebounceClick {
                ViewUtils.hideKeyboard(view)
                closeListener()
            }
            view.setBackgroundColor(attrData(R.attr.colorPrimary))
        }

        view.questName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                val text = s.toString()
                if (text.isBlank() || text.length == 1) {
                    setIcon(GoogleMaterial.Icon.gmd_send, view.done)
                } else {
                    setIcon(
                        GoogleMaterial.Icon.gmd_send,
                        view.done,
                        true,
                        attrData(R.attr.colorAccent)
                    )
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        view.questName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onSaveQuest(view)
            }
            true
        }

        view.done.onDebounceClick {
            onSaveQuest(view)
        }

        resetForm(view)

        return view
    }

    override fun colorStatusBars() {
        if (isFullscreen) {
            activity?.window?.statusBarColor = attrData(R.attr.colorPrimary)
        }
    }

    private fun setColor(color: Color, view: View) {
        val d = view.color.drawable as GradientDrawable
        d.setColor(colorRes(color.androidColor.color500))
    }

    private fun setIcon(
        icon: IIcon,
        view: ImageView,
        isSelected: Boolean = false,
        @ColorInt selectedColor: Int? = null
    ) {
        val color =
            if (isSelected)
                selectedColor ?: colorRes(colorTextHintResource)
            else
                colorRes(colorTextPrimaryResource)

        val iconDrawable =
            IconicsDrawable(activity!!)
                .icon(icon)
                .color(color)
                .sizeDp(24)

        view.setImageDrawable(iconDrawable)
    }

    private fun onSaveQuest(view: View) {
        dispatch(
            AddQuestAction.Save(
                name = view.questName.text.toString()
            )
        )
    }

    override fun onCreateLoadAction() = AddQuestAction.Load(currentDate)

    override fun render(state: AddQuestViewState, view: View) {

        setupFullAdd(view, state)

        when (state.type) {
            DATA_LOADED -> {
                renderDate(view, state)
                renderStartTime(view, state)
                renderDuration(state, view)
                renderColor(view, state)
                renderIcon(view, state)
                renderTags(view, state)
            }

            DATE_PICKED -> renderDate(view, state)

            TIME_PICKED -> renderStartTime(view, state)

            DURATION_PICKED -> renderDuration(state, view)

            TAGS_PICKED -> {
                renderTags(view, state)
                renderColor(view, state)
                renderIcon(view, state)
            }

            COLOR_PICKED -> renderColor(view, state)

            ICON_PICKED -> renderIcon(view, state)

            VALIDATION_ERROR_EMPTY_NAME ->
                view.questName.error = stringRes(R.string.think_of_a_name)

            QUEST_SAVED -> {
                showShortToast(R.string.quest_added)

                renderDate(view, state)
                renderStartTime(view, state)
                renderDuration(state, view)
                renderColor(view, state)
                renderIcon(view, state)
                renderTags(view, state)
                resetForm(view)
            }

            else -> {
            }
        }
    }

    private fun renderTags(view: View, state: AddQuestViewState) {
        setIcon(MaterialDesignIconic.Icon.gmi_label, view.tags, state.tags.isNotEmpty())
        view.tags.onDebounceClick {
            navigate()
                .toTagPicker(state.tags.toSet()) { tags ->
                    dispatch(AddQuestAction.TagsPicked(tags))
                }
        }
    }

    private fun setupFullAdd(
        view: View,
        state: AddQuestViewState
    ) {
        if (isFullscreen) {
            view.fullAdd.invisible()
        } else {
            view.fullAdd.onDebounceClick {
                closeListener()
                ViewUtils.hideKeyboard(view)
                navigateFromRoot()
                    .toEditQuest(
                        questId = null,
                        params = EditQuestViewController.Params(
                            name = view.questName.text.toString(),
                            scheduleDate = state.date,
                            startTime = state.time,
                            duration = state.duration,
                            color = state.color,
                            icon = state.icon,
                            reminderViewModel = null
                        )
                        , changeHandler = FadeChangeHandler()
                    )
            }
        }
    }

    private fun renderDate(
        view: View,
        state: AddQuestViewState
    ) {
        setIcon(GoogleMaterial.Icon.gmd_event, view.scheduleDate, state.date != null)
        view.scheduleDate.onDebounceClick {
            val date = state.date ?: LocalDate.now()
            val datePickerDialog = DatePickerDialog(
                view.context,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    dispatch(AddQuestAction.DatePicked(LocalDate.of(year, month + 1, dayOfMonth)))
                }, date.year, date.month.value - 1, date.dayOfMonth
            )
            datePickerDialog.setButton(
                Dialog.BUTTON_NEUTRAL,
                view.context.getString(R.string.do_not_know)
            ) { _, _ ->
                dispatch(AddQuestAction.DatePicked(null))
            }
            datePickerDialog.setOnCancelListener {
                dispatch(AddQuestAction.DatePickerCanceled)
            }
            datePickerDialog.show()
        }
    }

    private fun renderDuration(state: AddQuestViewState, view: View) {
        setIcon(GoogleMaterial.Icon.gmd_timer, view.duration, state.duration != null)
        view.duration.onDebounceClick {
            navigate()
                .toDurationPicker(
                    state.duration?.minutes
                ) { dispatch(AddQuestAction.DurationPicked(it.intValue)) }
        }
    }

    private fun renderIcon(
        view: View,
        state: AddQuestViewState
    ) {

        if (state.icon != null) {
            val androidIcon = state.icon.androidIcon
            val iconDrawable =
                IconicsDrawable(activity!!)
                    .icon(androidIcon.icon)
                    .color(colorRes(androidIcon.color))
                    .sizeDp(24)

            view.icon.setImageDrawable(iconDrawable)
        } else {
            setIcon(
                icon = GoogleMaterial.Icon.gmd_local_florist,
                view = view.icon,
                isSelected = false
            )
        }
        view.icon.onDebounceClick {
            navigate().toIconPicker({ icon ->
                dispatch(AddQuestAction.IconPicked(icon))
            }, state.icon)
        }
    }

    private fun renderColor(
        view: View,
        state: AddQuestViewState
    ) {
        state.color?.let {
            setColor(state.color, view)
        }
        view.color.onDebounceClick {
            navigate()
                .toColorPicker(
                    {
                        dispatch(AddQuestAction.ColorPicked(it))
                    }, state.color
                )
        }
    }

    private fun renderStartTime(
        view: View,
        state: AddQuestViewState
    ) {
        setIcon(GoogleMaterial.Icon.gmd_access_time, view.startTime, state.time != null)
        view.startTime.onDebounceClick {
            ViewUtils.hideKeyboard(view)
            val startTime = state.time ?: Time.now()

            val dialog = createTimePickerDialog(
                startTime = startTime,
                onTimePicked = {
                    dispatch(AddQuestAction.TimePicked(it))
                }
            )
            dialog.setOnDismissListener {
                view.postDelayed({
                    view.questName.requestFocus()
                    ViewUtils.showKeyboard(view.questName.context, view.questName)
                }, 10)
            }
            dialog.show(router)
        }
    }

    private fun resetForm(view: View) {
        view.questName.setText("")
        setIcon(GoogleMaterial.Icon.gmd_event, view.scheduleDate, true)
        setIcon(GoogleMaterial.Icon.gmd_access_time, view.startTime)
        setIcon(GoogleMaterial.Icon.gmd_timer, view.duration)
        setIcon(MaterialDesignIconic.Icon.gmi_label, view.tags)
        setIcon(GoogleMaterial.Icon.gmd_local_florist, view.icon)
        setColor(Color.GREEN, view)
        setIcon(GoogleMaterial.Icon.gmd_send, view.done)
        view.questName.requestFocus()
    }

    override fun onDetach(view: View) {
        view.questName.setOnEditTextImeBackListener(null)
        super.onDetach(view)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        view.postDelayed({
            view.questName.requestFocus()
            ViewUtils.showKeyboard(view.questName.context, view.questName)
        }, shortAnimTime)
    }
}