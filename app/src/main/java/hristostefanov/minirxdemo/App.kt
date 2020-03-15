package hristostefanov.minirxdemo

import android.app.Application
import hristostefanov.minirxdemo.util.ApplicationComponent
import hristostefanov.minirxdemo.util.DaggerApplicationComponent

class App: Application() {
    companion object {
        lateinit var instance: App
    }

    init {
        instance = this
    }

    val component: ApplicationComponent = DaggerApplicationComponent.create()
}