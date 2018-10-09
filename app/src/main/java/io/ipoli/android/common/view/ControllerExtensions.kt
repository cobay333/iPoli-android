package io.ipoli.android.common.view

import android.support.annotation.*
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import io.ipoli.android.MainActivity
import io.ipoli.android.R
import io.ipoli.android.common.navigation.Navigator

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/7/17.
 */
fun Controller.stringRes(@StringRes stringRes: Int): String =
    activity!!.stringRes(stringRes)

fun Controller.stringRes(@StringRes stringRes: Int, vararg formatArgs: Any): String =
    activity!!.stringRes(stringRes, *formatArgs)

fun Controller.stringsRes(@ArrayRes stringArrayRes: Int): List<String> =
    activity!!.stringsRes(stringArrayRes)

fun Controller.colorRes(@ColorRes colorRes: Int): Int =
    activity!!.colorRes(colorRes)

fun Controller.intRes(@IntegerRes res: Int): Int =
    activity!!.intRes(res)

fun Controller.quantityString(@PluralsRes res: Int, quantity: Int) =
    activity!!.quantityString(res, quantity)

val Controller.shortAnimTime: Long
    get() = resources!!.getInteger(android.R.integer.config_shortAnimTime).toLong()

val Controller.mediumAnimTime: Long
    get() = resources!!.getInteger(android.R.integer.config_mediumAnimTime).toLong()

val Controller.longAnimTime: Long
    get() = resources!!.getInteger(android.R.integer.config_longAnimTime).toLong()

fun Controller.showBackButton() {
    (activity!! as MainActivity).showBackButton()
}

fun Controller.pushWithRootRouter(transaction: RouterTransaction) =
    (activity!! as MainActivity).pushWithRootRouter(transaction)

val Controller.rootRouter get() = (activity!! as MainActivity).rootRouter

fun Controller.attrData(@AttrRes attributeRes: Int) =
    activity!!.attrData(attributeRes)

fun Controller.attrResourceId(@AttrRes attributeRes: Int) =
    TypedValue().let {
        activity!!.theme.resolveAttribute(attributeRes, it, true)
        it.resourceId
    }

val Controller.colorTextPrimaryResource
    get() = attrResourceId(android.R.attr.textColorPrimary)

val Controller.colorTextSecondaryResource
    get() = attrResourceId(android.R.attr.textColorSecondary)

val Controller.colorTextHintResource
    get() = attrResourceId(android.R.attr.textColorHint)

val Controller.colorDividerResource
    get() = attrResourceId(android.R.attr.listDivider)

val Controller.colorSurfaceResource
    get() = attrResourceId(R.attr.colorSurface)

val Controller.colorBackgroundResource
    get() = attrResourceId(android.R.attr.colorBackground)

val Controller.colorAccent
    get() = attrData(R.attr.colorAccent)

var Controller.toolbarTitle: String
    set(value) {
        (activity!! as MainActivity).supportActionBar!!.title = value
    }
    get() = (activity!! as MainActivity).supportActionBar!!.title.toString()

val Controller.toolbar: Toolbar
    get() = activity!!.findViewById(R.id.toolbar)

fun Controller.setToolbar(toolbar: Toolbar) {
    (activity!! as MainActivity).setSupportActionBar(toolbar)
}

fun Controller.addToolbarView(@LayoutRes viewLayout: Int): View? =
    activity?.let {
        it.layoutInflater.inflate(viewLayout, toolbar, false).also {
            toolbar.addView(it)
        }
    }

fun Controller.removeToolbarView(view: View) {
    toolbar.removeView(view)
}

fun Controller.showShortToast(text: String) {
    Toast.makeText(activity!!, text, Toast.LENGTH_SHORT).show()
}

fun Controller.showShortToast(@StringRes text: Int) {
    Toast.makeText(activity!!, text, Toast.LENGTH_SHORT).show()
}

fun Controller.showLongToast(text: String) {
    Toast.makeText(activity!!, text, Toast.LENGTH_LONG).show()
}

fun Controller.showLongToast(@StringRes text: Int) {
    Toast.makeText(activity!!, text, Toast.LENGTH_LONG).show()
}

fun Controller.enterFullScreen() {
    activity?.let {
        it.window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
    }
}

fun Controller.exitFullScreen() {
    activity?.let { it.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE }
}

fun Controller.setChildController(view: ViewGroup, controller: Controller) {
    val childRouter = getChildRouter(view)

    if (!childRouter.hasRootController()) {
        childRouter.setRoot(RouterTransaction.with(controller))
    }
}

fun Controller.navigate(): Navigator = Navigator(router)