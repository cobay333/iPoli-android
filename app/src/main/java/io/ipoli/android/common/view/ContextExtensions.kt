package io.ipoli.android.common.view

import android.content.Context
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import android.support.annotation.*
import android.support.v4.content.ContextCompat
import android.support.v7.view.ContextThemeWrapper
import android.util.TypedValue
import io.ipoli.android.Constants
import io.ipoli.android.player.Theme

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 15.12.17.
 */

val Context.playerTheme: Int
    get() {
        val pm = PreferenceManager.getDefaultSharedPreferences(this)
        if (pm.contains(Constants.KEY_THEME)) {
            val themeName = pm.getString(Constants.KEY_THEME, "")
            if (themeName.isNotEmpty()) {
                return AndroidTheme.valueOf(themeName).style
            }
        }
        return AndroidTheme.valueOf(Constants.DEFAULT_THEME.name).style
    }

val Context.isDarkTheme: Boolean
    get() {
        val pm = PreferenceManager.getDefaultSharedPreferences(this)
        if (pm.contains(Constants.KEY_THEME)) {
            val themeName = pm.getString(Constants.KEY_THEME, "")
            if (themeName.isNotEmpty()) {
                return Theme.valueOf(themeName).isDark
            }
        }
        return Constants.DEFAULT_THEME.isDark
    }

fun Context.asThemedWrapper() =
    ContextThemeWrapper(this, playerTheme)

fun Context.attrData(@AttrRes attributeRes: Int) =
    TypedValue().let {
        theme.resolveAttribute(attributeRes, it, true)
        it.data
    }

fun Context.isAppInstalled(packageName: String) =
    try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

fun Context.stringRes(@StringRes stringRes: Int): String =
    resources!!.getString(stringRes)

fun Context.stringRes(@StringRes stringRes: Int, vararg formatArgs: Any): String =
    resources!!.getString(stringRes, *formatArgs)

fun Context.stringsRes(@ArrayRes stringArrayRes: Int): List<String> =
    resources!!.getStringArray(stringArrayRes).toList()

fun Context.colorRes(@ColorRes colorRes: Int): Int =
    ContextCompat.getColor(this, colorRes)

fun Context.intRes(@IntegerRes res: Int): Int =
    resources!!.getInteger(res)

fun Context.quantityString(@PluralsRes res: Int, quantity: Int) =
    resources!!.getQuantityString(res, quantity, quantity)
