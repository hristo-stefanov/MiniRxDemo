package hristostefanov.minirxdemo.data

import hristostefanov.minirxdemo.business.Post
import hristostefanov.minirxdemo.business.Repository
import hristostefanov.minirxdemo.persistence.PersistedDataSource
import hristostefanov.minirxdemo.remote.Service
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val service: Service,
    private val persistentDataSource: PersistedDataSource
) : Repository {

    override fun getAllPosts(): Observable<List<Post>> {
        return persistentDataSource.getAllPosts()
    }

    override fun refresh(): Completable {
        return service.getAllPosts().flatMap { posts ->
            service.getAllUsers().map { users ->
                Pair(posts, users)
            }
        }.flatMapCompletable {
            Completable.fromRunnable {
                persistentDataSource.refreshInTx(it.first, it.second)
            }
        }
    }
}