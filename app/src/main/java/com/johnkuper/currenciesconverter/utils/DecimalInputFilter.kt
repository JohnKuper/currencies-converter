package com.johnkuper.currenciesconverter.utils

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Pattern

class DecimalInputFilter(beforeZero: Int, afterZero: Int) : InputFilter {

    private var pattern: Pattern
    private val separators = "[\\.\\,]"

    init {
        val patterBefore = "(\\d{1,$beforeZero})"
        val patterAfter = "(\\d{1,$afterZero})"
        val numberRegex = StringBuilder()
            .append("($patterBefore$separators$patterAfter)")
            .append("|")
            .append("($patterBefore$separators)")
            .append("|")
            .append("($patterBefore)")
            .toString()

        pattern = Pattern.compile(numberRegex)
    }

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val input = dest.substring(0, dstart) + source.substring(start, end) + dest.substring(dend)
        val matcher = pattern.matcher(input)
        return if (!matcher.matches()) "" else null
    }
}