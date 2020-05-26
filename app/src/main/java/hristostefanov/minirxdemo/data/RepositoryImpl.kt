package hristostefanov.minirxdemo.data

import hristostefanov.minirxdemo.business.DataSource
import hristostefanov.minirxdemo.business.Post
import hristostefanov.minirxdemo.business.Repository
import hristostefanov.minirxdemo.business.User
import hristostefanov.minirxdemo.persistence.PersistedDataSource
import hristostefanov.minirxdemo.remote.RemoteDataSource
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Named

// See
// https://blog.danlew.net/2015/06/22/loading-data-from-multiple-sources-with-rxjava/
// https://medium.com/@jaerencoathup/repository-pattern-using-rxjava-and-room-4ce79e4ffc5c
// https://medium.com/@rikvanv/android-repository-pattern-using-room-retrofit2-and-rxjava2-b48aedd173c
// https://medium.com/corebuild-software/android-repository-pattern-using-rx-room-bac6c65d7385
// https://www.bignerdranch.com/blog/the-rxjava-repository-pattern/


class RepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val persistentDataSource: PersistedDataSource
): Repository {
    override fun getAllPosts(): Single<List<Post>> {
        return Observable.concat(
            persistentDataSource.getAllPosts().filter{ it.isNotEmpty() }.toObservable(),
            remoteDataSource.getAllPosts().toObservable().doOnNext {
                persistentDataSource.savePosts(it)
            }
        ).firstElement().toSingle().subscribeOn(Schedulers.io())
    }

    override fun getUserById(userId: Int): Single<User> {
        return Observable.concat(persistentDataSource.getUserById(userId).toObservable(),
            remoteDataSource.getUserById(userId).doOnSuccess {
                persistentDataSource.saveUser(it)
            }.toObservable()).firstElement().toSingle().subscribeOn(Schedulers.io())
    }
}