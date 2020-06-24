package hristostefanov.minirxdemo.business

import hristostefanov.minirxdemo.persistence.Database
import hristostefanov.minirxdemo.remote.PostDTO
import hristostefanov.minirxdemo.remote.Service
import hristostefanov.minirxdemo.remote.UserDTO
import io.reactivex.Completable
import javax.inject.Inject

class RefreshInteractor @Inject constructor(
    // TODO depending on external service, make a gateway?
    private val service: Service,
    private val userGateway: UserGateway,
    private val postGateway: PostGateway,
    private val database: Database
) {
    fun execute(): Completable {
        return service.getAllPosts().flatMap { posts ->
            service.getAllUsers().map { users ->
                Pair(posts, users)
            }
        }.flatMapCompletable {
            Completable.fromRunnable {
                refreshInTx(it.first, it.second)
            }
        }
    }

    private fun refreshInTx(first: List<PostDTO>, second: List<UserDTO>) {
        // TODO: abstract running transaction without depending on the database
        database.runInTransaction {
            userGateway.deleteAll()
            postGateway.deleteAll()
            userGateway.insert(second.map {
                User(it.id, it.username)
            })
            postGateway.insertAll(first.map {
                Post(it.id, it.title, it.body, it.userId)
            })
        }
    }

}