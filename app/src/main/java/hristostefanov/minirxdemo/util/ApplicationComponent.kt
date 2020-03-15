package hristostefanov.minirxdemo.util

import dagger.Component
import hristostefanov.minirxdemo.presentation.MainViewModel
import hristostefanov.minirxdemo.ui.ViewModelFactory

@ApplicationScope
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun getViewModelFactory(): ViewModelFactory
    fun getMainViewModel(): MainViewModel
}