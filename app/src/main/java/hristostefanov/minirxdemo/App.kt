package hristostefanov.minirxdemo

import android.app.Application
import hristostefanov.minirxdemo.utilities.di.ApplicationComponent
import hristostefanov.minirxdemo.utilities.di.DaggerApplicationComponent

class App: Application() {
    val component: ApplicationComponent = DaggerApplicationComponent.factory().create(this)

    override fun onCreate() {
        super.onCreate()
        component.getStartup().execute()
    }

    override fun onTerminate() {
        component.getStartup().exit()
        super.onTerminate()
    }
}