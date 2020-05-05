package hristostefanov.minirxdemo.util

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import hristostefanov.minirxdemo.presentation.MainViewModel
import hristostefanov.minirxdemo.ui.ViewModelFactory

@ApplicationScope
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: Application): ApplicationComponent
    }

    fun getViewModelFactory(): ViewModelFactory
    fun getMainViewModel(): MainViewModel
}