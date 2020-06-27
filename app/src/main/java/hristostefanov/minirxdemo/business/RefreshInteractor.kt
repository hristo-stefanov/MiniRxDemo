package hristostefanov.minirxdemo.business

import hristostefanov.minirxdemo.persistence.*
import hristostefanov.minirxdemo.remote.PostDTO
import hristostefanov.minirxdemo.remote.Service
import hristostefanov.minirxdemo.remote.UserDTO
import io.reactivex.Completable
import javax.inject.Inject

class RefreshInteractor @Inject constructor(
    // TODO depending on external service, make a gateway with special error handling??
    private val service: Service,
    private val userGateway: UserDAO,
    private val postGateway: PostDAO,
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
                UserEntity(it.id, it.username)
            })
            postGateway.insert(first.map {
                PostEntity(it.id, it.title, it.body, it.userId)
            })
        }
    }

}