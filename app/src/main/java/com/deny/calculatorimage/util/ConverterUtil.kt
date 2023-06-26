package com.deny.calculatorimage.util

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue
import java.text.DecimalFormat

object ConverterUtil {

    private const val ONE_GIGABYTE = 1048576F

    fun dpToPx(context: Context, dp: Float): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun spToPx(context: Context, sp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)
    }

    fun pxToDp(context: Context, px: Float): Float {
        return px * (DisplayMetrics.DENSITY_DEFAULT / context.resources.displayMetrics.densityDpi.toFloat())
    }

    fun convertVoice(voice: Int): Int {
        return voice / 60
    }

    fun revertVoice(voice: Int): Int {
        return voice * 60
    }

    fun convertDelimitedNumber(number: Long, useDot: Boolean = false): String {
        var value = DecimalFormat("#,###").format(number)

        if (useDot) {
            value = value.replace(",", ".")
        }

        return value
    }

    fun getInitialName(name: String): String {
        var lastLetter = ""
        var firstLetter = ""
        var initial = ""
        if (name.isNotEmpty()) {
            firstLetter = name.substring(0, 1).toUpperCase()
        }

        if (name.isNotEmpty() && name.split(" ").size > 1) {
            lastLetter = try {
                name.substring(name.lastIndexOf(" ") + 1, name.lastIndexOf(" ") + 2).toUpperCase()
            } catch (err: StringIndexOutOfBoundsException) {
                ""
            }
        }
        initial = firstLetter + lastLetter
        return initial
    }


    fun convertToShortenedDelimitedNumber(value:Long, useDot: Boolean = false):String{
        return if(value>=1000)
            convertDelimitedNumber((value/1000), useDot)+"K"
        else
            convertDelimitedNumber(value, useDot)
    }

    fun Int.dp(context: Context): Int {
        return this * context.resources.displayMetrics.density.toInt()
    }
}