package hristostefanov.minirxdemo.data

import hristostefanov.minirxdemo.business.Post
import hristostefanov.minirxdemo.business.Repository
import hristostefanov.minirxdemo.business.User
import hristostefanov.minirxdemo.persistence.PersistedDataSource
import hristostefanov.minirxdemo.remote.RemoteDataSource
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val persistentDataSource: PersistedDataSource
) : Repository {

    override fun getAllPosts(): Observable<List<Post>> {
        return persistentDataSource.getAllPosts()
    }

    override fun getUserById(userId: Int): Single<User> {
        return persistentDataSource.getUserById(userId)
    }

    override fun refresh(): Completable {
        // TODO fetch only needed Users
        return remoteDataSource.getAllPosts().flatMap { posts ->
            remoteDataSource.getAllUsers().map { users ->
                Pair(posts, users)
            }
        }.flatMapCompletable {
            Completable.fromRunnable {
                persistentDataSource.refreshTx(it.first, it.second)
            }
        }
    }
}