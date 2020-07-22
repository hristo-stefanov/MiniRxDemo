package hristostefanov.minirxdemo.utilities

import androidx.annotation.StringRes

interface StringSupplier {
    fun get(@StringRes resId: Int): String
}