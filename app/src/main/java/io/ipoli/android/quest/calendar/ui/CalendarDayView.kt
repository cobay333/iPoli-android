package io.ipoli.android.quest.calendar.ui

import android.content.Context
import android.content.res.Resources
import android.database.DataSetObserver
import android.graphics.drawable.Drawable
import android.support.transition.TransitionManager
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import io.ipoli.android.R
import io.ipoli.android.common.datetime.Time
import kotlinx.android.synthetic.main.calendar_hour_cell.view.*
import kotlinx.android.synthetic.main.view_calendar_day.view.*

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/2/17.
 */
class CalendarDayView : LinearLayout {

    private val MIN_EVENT_DURATION = 10
    private val MAX_EVENT_DURATION = Time.h2Min(4)
    private var hourHeight: Float = 0f
    private var minuteHeight: Float = 0f
    private lateinit var dragImage: Drawable
    private var dragImageSize: Int = toPx(16)
    private val adapterViews = mutableListOf<View>()

    private enum class Mode {
        NONE,
        DRAG,
        ZOOM
    }

    private lateinit var editModeBackground: View
    private lateinit var topDragView: View
    private lateinit var bottomDragView: View
    private var mode = Mode.NONE
    private var startY = 0f
    private var dy = 0f
    private var adapter: CalendarAdapter<*>? = null

    private val dataSetObserver = object : DataSetObserver() {
        override fun onChanged() {
            refreshViewsFromAdapter()
        }

        override fun onInvalidated() {
            removeAllViews()
        }
    }

    constructor(context: Context) : super(context) {
        initUi(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initUi(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initUi(attrs, defStyleAttr)
    }

    private fun initUi(attrs: AttributeSet?, defStyleAttr: Int) {
        LayoutInflater.from(context).inflate(R.layout.view_calendar_day, this, true)

        orientation = VERTICAL

        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(attrs, R.styleable.CalendarDayView, defStyleAttr, 0)
            dragImage = a.getDrawable(R.styleable.CalendarDayView_dragImage)
            dragImageSize = a.getDimensionPixelSize(R.styleable.CalendarDayView_dragImageSize, dragImageSize)
            a.recycle()
        }
        val screenHeight = getScreenHeight()
        hourHeight = screenHeight / 6f
        minuteHeight = hourHeight / 60f

        scrollView.isVerticalScrollBarEnabled = false

        setupHourCells()
        setupEditBackgroundView()

        topDragView = addDragView()
        bottomDragView = addDragView()
    }

    private fun setupEditBackgroundView() {
        editModeBackground = View(context)
        editModeBackground.layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        editModeBackground.setBackgroundResource(R.color.md_dark_text_26)
        editModeBackground.visibility = View.GONE
        eventContainer.addView(editModeBackground)
    }

    private fun setupHourCells() {
        for (hour in 0..23) {
            val hourView = LayoutInflater.from(context).inflate(R.layout.calendar_hour_cell, this, false)
            if (hour > 0) {
                hourView.timeLabel.text = hour.toString()
            }
            val layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, hourHeight.toInt())
            layoutParams.topMargin = (hour * hourHeight).toInt()
            hourView.layoutParams = layoutParams
            eventContainer.addView(hourView)
        }
    }

    private fun addDragView(): View {
        val view = ImageView(context)
        view.layoutParams = LayoutParams(dragImageSize, dragImageSize)
        view.setImageDrawable(dragImage)
        view.visibility = View.GONE
        eventContainer.addView(view)
        return view
    }

    fun setAdapter(adapter: CalendarAdapter<*>) {
        this.adapter?.unregisterDataSetObserver(dataSetObserver)
        this.adapter = adapter
        this.adapter?.registerDataSetObserver(dataSetObserver)
        initViewsFromAdapter()
    }

    private fun initViewsFromAdapter() {
//        removeAllViews()
        val a = adapter!!
        for (i in 0 until a.count) {
            val adapterView = a.getView(i, null, eventContainer)
            val event = a.getItem(i)
            adapterView.setPositionAndHeight(
                event.startMinute * minuteHeight,
                (event.duration * minuteHeight).toInt())
            adapterViews.add(i, adapterView)
            eventContainer.addView(adapterView)
        }
    }

    private fun refreshViewsFromAdapter() {
        val childCount = childCount
        val a = adapter!!
        val adapterSize = a.count
        val reuseCount = Math.min(childCount, adapterSize)

        for (i in 0 until reuseCount) {
            a.getView(i, getChildAt(i), eventContainer)
        }

        if (childCount < adapterSize) {
            for (i in childCount until adapterSize) {
                eventContainer.addView(a.getView(i, null, eventContainer), i)
            }
        } else if (childCount > adapterSize) {
            removeViews(adapterSize, childCount)
        }
    }

    private fun getScreenHeight(): Int {
        val metrics = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)
        return metrics.heightPixels
    }

    private fun getHoursFor(y: Float): Int {
        val h = getRelativeY(y) / hourHeight
        return Math.min(h.toInt(), 23)
    }

    private fun getMinutesFor(y: Float, rangeLength: Int): Int {
        var minutes = (getRelativeY(y) % hourHeight / minuteHeight).toInt()
        minutes = Math.max(0, minutes)
        val bounds = mutableListOf<Int>()
        var rangeStart = 0
        for (min in 0..59) {
            if (min % rangeLength == 0) {
                rangeStart = min
            }
            bounds.add(rangeStart)
        }
        return bounds[minutes]
    }

    private fun getRelativeY(y: Float): Float {
        val offsets = IntArray(2)
        eventContainer.getLocationOnScreen(offsets)
        return getRelativeY(y, offsets[1].toFloat())
    }

    private fun getRelativeY(y: Float, yOffset: Float): Float =
        Math.max(0f, scrollY + y - yOffset)

    private fun getYPositionFor(time: Time): Float =
        time.hours * hourHeight + getMinutesHeight(time.getMinutes())

    //    private fun getYPositionFor(minutesAfterMidnight: Int): Float {
//        val time = Time.of(minutesAfterMidnight)
//        return getYPositionFor(time.hours, time.getMinutes())
//    }
//
//    protected fun getHeightFor(duration: Int): Int {
//        return getMinutesHeight(duration).toInt()
//    }
    private fun getMinutesHeight(minutes: Int): Float =
        minuteHeight * minutes

    private fun getMinutesFor(height: Int): Int =
        (height / minuteHeight).toInt()

    fun startEditMode(editView: View, position: Int) {
        scrollView.locked = true
        editModeBackground.bringToFront()
        editView.bringToFront()
        setupTopDragView(editView)
        setupBottomDragView(editView)
        TransitionManager.beginDelayedTransition(this)
        showViews(editModeBackground, topDragView, bottomDragView)

        startY = -1f
        setEditViewTouchListener(editView, position)

        adapter?.onStartEdit(editView, position)
    }

    private fun setupBottomDragView(editView: View) {
        bottomDragView.bringToFront()
        positionBottomDragView(editView)
        setBottomDragViewListener(bottomDragView, editView)
    }

    private fun setupTopDragView(editView: View) {
        topDragView.bringToFront()
        positionTopDragView(editView)
        setTopDragViewListener(topDragView, editView)
    }

    private fun positionBottomDragView(editView: View) {
        val lp = bottomDragView.layoutParams as MarginLayoutParams
        lp.topMargin = editView.bottom - dragImageSize / 2
        lp.marginStart = editView.left + editView.width / 2
        bottomDragView.layoutParams = lp
    }

    private fun positionTopDragView(editView: View) {
        val lp = topDragView.layoutParams as MarginLayoutParams
        lp.topMargin = editView.top - dragImageSize / 2
        lp.marginStart = editView.left + editView.width / 2
        topDragView.layoutParams = lp
    }

    private fun setEditViewTouchListener(editView: View, position: Int) {
        editView.setOnTouchListener { _, e ->
            editModeBackground.setOnTouchListener { _, _ ->
                stopEditMode(editView, position)
                true
            }

            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    mode = Mode.DRAG
                    startY = e.y
                }
                MotionEvent.ACTION_MOVE -> {
                    if (startY < 0) {
                        startY = e.y
                    }
                    dy = e.y - startY
                    onChangeEditViewPosition(editView, dy)
                    adapter?.onStartTimeChanged(editView, position, getStartTimeFromPosition(editView, 5))
                }
                MotionEvent.ACTION_UP -> {
                    mode = Mode.NONE
                }
                else -> {
                }
            }
            true
        }
    }

    private fun stopEditMode(editView: View, position: Int) {
        scrollView.locked = false
        editView.setPosition(getAdjustedYPosFor(editView, rangeLength = 5))
        editView.setOnTouchListener(null)
        editModeBackground.setOnTouchListener(null)
        TransitionManager.beginDelayedTransition(this)
        hideViews(editModeBackground, topDragView, bottomDragView)
        adapter?.onStopEdit(editView, position)
    }

    private fun onChangeEditViewPosition(editView: View, deltaY: Float) {
        editView.changePosition(deltaY)
        topDragView.changePosition(deltaY)
        bottomDragView.changePosition(deltaY)
    }

    private fun setBottomDragViewListener(bottomDragView: View, editView: View) {
        var lastY = 0f
        bottomDragView.setOnTouchListener { _, e ->
            if (e.actionMasked == MotionEvent.ACTION_DOWN) {
                lastY = e.y
            }

            if (e.actionMasked == MotionEvent.ACTION_MOVE) {
                dy = e.y - lastY
                val height = editView.height + dy.toInt()
                if (isValidHeightForEvent(height)) {
                    editView.changeHeight(height)
                    bottomDragView.changePosition(dy)
                    lastY = e.y
                }
            }

            true
        }
    }

    private fun setTopDragViewListener(topDragView: View, editView: View) {
        var lastY = 0f
        topDragView.setOnTouchListener { _, e ->
            if (e.actionMasked == MotionEvent.ACTION_DOWN) {
                lastY = e.y
            }

            if (e.actionMasked == MotionEvent.ACTION_MOVE) {
                dy = e.y - lastY
                val height = editView.height - dy.toInt()
                if (isValidHeightForEvent(height)) {
                    editView.changePositionAndHeight(dy, height)
                    topDragView.changePosition(dy)
                    lastY = e.y
                }
            }

            true
        }
    }

    private fun View.getRawTop(): Float {
        val loc = IntArray(2)
        getLocationInWindow(loc)
        return loc[1].toFloat()
    }

    private fun View.changePositionAndHeight(yDelta: Float, height: Int) =
        changeLayoutParams<MarginLayoutParams> {
            it.topMargin += yDelta.toInt()
            it.height = height
        }

    private fun View.setPositionAndHeight(yPosition: Float, height: Int) =
        changeLayoutParams<MarginLayoutParams> {
            it.topMargin = yPosition.toInt()
            it.height = height
        }

    private fun View.setPosition(yPosition: Float) =
        changeLayoutParams<MarginLayoutParams> { it.topMargin = yPosition.toInt() }

    private fun View.changePosition(yDelta: Float) =
        changePosition(yDelta.toInt())

    private fun View.changePosition(yDelta: Int) =
        changeLayoutParams<MarginLayoutParams> { it.topMargin += yDelta }

    private fun View.changeHeight(height: Int) {
        changeLayoutParams<MarginLayoutParams> { it.height = height }
    }

    private fun <T : ViewGroup.LayoutParams> View.changeLayoutParams(cb: (layoutParams: T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val lp = layoutParams as T
        cb(lp)
        layoutParams = lp
    }

    private fun isValidHeightForEvent(height: Int): Boolean =
        getMinutesFor(height) in MIN_EVENT_DURATION..MAX_EVENT_DURATION

    private fun getAdjustedYPosFor(view: View, rangeLength: Int): Float =
        getYPositionFor(getStartTimeFromPosition(view, rangeLength))

    private fun getStartTimeFromPosition(view: View, rangeLength: Int): Time {
        val rawTop = view.getRawTop()
        return Time.at(
            hours = getHoursFor(rawTop),
            minutes = getMinutesFor(rawTop, rangeLength)
        )
    }

    private fun showViews(vararg views: View) =
        views.forEach { it.visibility = View.VISIBLE }

    private fun hideViews(vararg views: View) =
        views.forEach { it.visibility = View.GONE }

    private fun toPx(dp: Int): Int =
        (dp * Resources.getSystem().displayMetrics.density).toInt()
}