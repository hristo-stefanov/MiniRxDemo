package hristostefanov.minirxdemo

import android.app.Application
import hristostefanov.minirxdemo.util.ApplicationComponent
import hristostefanov.minirxdemo.util.DaggerApplicationComponent

class App: Application() {
    val component: ApplicationComponent = DaggerApplicationComponent.factory().create(this)
}