package hristostefanov.minirxdemo.util;

import org.jetbrains.annotations.NotNull;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import hristostefanov.minirxdemo.App;
import hristostefanov.minirxdemo.BuildConfig;
import hristostefanov.minirxdemo.business.DataSource;
import hristostefanov.minirxdemo.remote.RemoteDataSource;
import hristostefanov.minirxdemo.remote.Service;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public abstract class ApplicationModule {
    @Binds
    abstract  DataSource bind(RemoteDataSource dataSource);

    @ApplicationScope
    @Provides
    static Retrofit provideRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVICE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    @Provides
    static Service provideService(Retrofit retrofit) {
        return retrofit.create(Service.class);
    }

    @ApplicationScope
    @Provides
    static StringSupplier provideStringSupplier() {
        return resId -> App.instance.getString(resId);
    }
}
