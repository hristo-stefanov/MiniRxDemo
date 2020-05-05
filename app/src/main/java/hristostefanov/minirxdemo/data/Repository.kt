package hristostefanov.minirxdemo.data

import hristostefanov.minirxdemo.business.DataSource
import hristostefanov.minirxdemo.business.Post
import hristostefanov.minirxdemo.remote.RemoteDataSource
import io.reactivex.Observable

// See
// https://medium.com/@jaerencoathup/repository-pattern-using-rxjava-and-room-4ce79e4ffc5c
// https://medium.com/@rikvanv/android-repository-pattern-using-room-retrofit2-and-rxjava2-b48aedd173c
// https://medium.com/corebuild-software/android-repository-pattern-using-rx-room-bac6c65d7385
// https://www.bignerdranch.com/blog/the-rxjava-repository-pattern/


class Repository(private val remoteDataSource: DataSource, private val persistentDataSource: DataSource) {
    fun getAllPosts(): Observable<List<Post>> {
        return Observable.concat(persistentDataSource.getAllPosts().toObservable(), remoteDataSource.getAllPosts().toObservable())
    }
}