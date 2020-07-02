package hristostefanov.minirxdemo.utilities.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import hristostefanov.minirxdemo.business.interactors.AutoRefreshLocalDataService
import hristostefanov.minirxdemo.presentation.MainViewModel

@ApplicationScope
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: Application): ApplicationComponent
    }

    fun getMainViewModel(): MainViewModel
    fun getAutoRefreshLocalDataService(): AutoRefreshLocalDataService
}