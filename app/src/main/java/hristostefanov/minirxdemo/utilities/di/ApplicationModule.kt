package hristostefanov.minirxdemo.utilities.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import hristostefanov.minirxdemo.BuildConfig
import hristostefanov.minirxdemo.utilities.db.Database
import hristostefanov.minirxdemo.business.gateways.local.PostDAO
import hristostefanov.minirxdemo.business.gateways.local.UserDAO
import hristostefanov.minirxdemo.business.gateways.remote.Service
import hristostefanov.minirxdemo.utilities.StringSupplier
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executor
import javax.inject.Named

@Module
abstract class ApplicationModule {
    companion object {

        @Provides
        fun providePostDAO(db: Database): PostDAO = db.postDao()

        @Provides
        fun provideUserDAO(db: Database): UserDAO = db.userDao()


        @Provides @Named("transactionExecutor")
        fun transactionExecutor(db: Database): Executor = Executor { command ->
            db.runInTransaction(command)
        }

        @ApplicationScope
        @Provides
        fun provideDatabase(app: Application): Database {
            return Room.databaseBuilder(app.applicationContext, Database::class.java, "db").build()
        }

        @ApplicationScope
        @Provides
        fun provideRetrofit(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BuildConfig.SERVICE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        }

        @Provides
        fun provideService(retrofit: Retrofit): Service {
            return retrofit.create(Service::class.java)
        }

        @ApplicationScope
        @Provides
        fun provideStringSupplier(app: Application): StringSupplier {
            // the provided implementation references the application context which is always
            // present during the life of the app process, hence no worries about leaks here
            return object : StringSupplier {
                override fun get(resId: Int): String = app.getString(resId)
            }
        }
    }
}