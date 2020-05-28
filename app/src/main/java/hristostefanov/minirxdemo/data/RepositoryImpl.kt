package hristostefanov.minirxdemo.data

import hristostefanov.minirxdemo.business.Post
import hristostefanov.minirxdemo.business.Repository
import hristostefanov.minirxdemo.business.User
import hristostefanov.minirxdemo.persistence.Database
import hristostefanov.minirxdemo.persistence.PersistedDataSource
import hristostefanov.minirxdemo.remote.RemoteDataSource
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

// See
// https://blog.danlew.net/2015/06/22/loading-data-from-multiple-sources-with-rxjava/
// https://medium.com/@jaerencoathup/repository-pattern-using-rxjava-and-room-4ce79e4ffc5c
// https://medium.com/@rikvanv/android-repository-pattern-using-room-retrofit2-and-rxjava2-b48aedd173c
// https://medium.com/corebuild-software/android-repository-pattern-using-rx-room-bac6c65d7385
// https://www.bignerdranch.com/blog/the-rxjava-repository-pattern/


class RepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val persistentDataSource: PersistedDataSource,
    private val database: Database
) : Repository {

    override fun getAllPosts(): Observable<List<Post>> {
        return persistentDataSource.getAllPosts()
    }

    override fun getUserById(userId: Int): Single<User> {
        return persistentDataSource.getUserById(userId)
    }

    override fun refresh(): Completable {
        // TODO fetch only needed Users
        // TODO consider error handling
        val pair: Single<Pair<List<Post>, List<User>>> = remoteDataSource.getAllPosts().flatMap { posts ->
            remoteDataSource.getAllUsers().map {users ->
                Pair(posts, users)
            }

        }
        return pair.doOnSuccess {
            database.runInTransaction {
                persistentDataSource.clear();
                persistentDataSource.savePosts(it.first)
                persistentDataSource.saveUsers(it.second)
            }
        }.ignoreElement()
    }
}