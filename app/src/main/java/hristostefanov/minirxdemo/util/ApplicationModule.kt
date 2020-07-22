package hristostefanov.minirxdemo.util

import android.app.Application
import dagger.Binds
import dagger.Module
import dagger.Provides
import hristostefanov.minirxdemo.BuildConfig
import hristostefanov.minirxdemo.business.DataSource
import hristostefanov.minirxdemo.remote.RemoteDataSource
import hristostefanov.minirxdemo.remote.Service
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@Module
abstract class ApplicationModule {
    @Binds
    abstract fun bindDataSource(dataSource: RemoteDataSource): DataSource

    companion object {
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