package hristostefanov.minirxdemo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import hristostefanov.minirxdemo.util.ApplicationComponent
import hristostefanov.minirxdemo.util.ApplicationScope
import hristostefanov.minirxdemo.presentation.MainViewModel
import java.lang.IllegalArgumentException
import javax.inject.Inject

@ApplicationScope
class ViewModelFactory @Inject constructor(private val applicationComponent: ApplicationComponent): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        when(modelClass) {
            MainViewModel::class.java -> applicationComponent.getMainViewModel() as T
            else -> throw IllegalArgumentException()
        }
}