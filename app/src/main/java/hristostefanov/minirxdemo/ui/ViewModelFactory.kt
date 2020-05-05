package hristostefanov.minirxdemo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import hristostefanov.minirxdemo.presentation.MainViewModel
import hristostefanov.minirxdemo.util.ApplicationComponent
import javax.inject.Inject

class ViewModelFactory @Inject constructor(private val applicationComponent: ApplicationComponent): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        when(modelClass) {
            MainViewModel::class.java -> applicationComponent.getMainViewModel() as T
            else -> throw IllegalArgumentException()
        }
}