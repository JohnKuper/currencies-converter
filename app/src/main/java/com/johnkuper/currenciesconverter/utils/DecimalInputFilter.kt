package com.johnkuper.currenciesconverter.utils

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Pattern

class DecimalInputFilter(beforeZero: Int, afterZero: Int) : InputFilter {

    //TODO Kuper replace . on ,
    private val pattern: Pattern = Pattern.compile(
        "(([1-9]{1})([0-9]{0," + (beforeZero - 1) + "})?)?(\\.[0-9]{0," + afterZero + "})?"
    )

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val sourceResult = source.subSequence(start, end).toString()
        val result = StringBuilder(dest)
            .replace(dstart, dend, sourceResult)
            .toString()

        return if (!pattern.matcher(result).matches()) "" else null
    }
}
