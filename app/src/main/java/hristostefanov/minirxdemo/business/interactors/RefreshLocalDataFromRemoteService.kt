package hristostefanov.minirxdemo.business.interactors

import hristostefanov.minirxdemo.business.entities.Post
import hristostefanov.minirxdemo.business.entities.User
import hristostefanov.minirxdemo.business.gateways.local.PostDAO
import hristostefanov.minirxdemo.business.gateways.local.UserDAO
import hristostefanov.minirxdemo.business.gateways.remote.PostResource
import hristostefanov.minirxdemo.business.gateways.remote.Service
import hristostefanov.minirxdemo.business.gateways.remote.UserResource
import io.reactivex.Completable
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Named

class RefreshLocalDataFromRemoteService @Inject constructor(
    private val service: Service,
    private val userDAO: UserDAO,
    private val postDAO: PostDAO,
    @Named("transactionExecutor")
    private val transactionExecutor: Executor
) {
    val execution: Completable by lazy {
        service.getAllPosts().flatMap { posts ->
            service.getAllUsers().map { users ->
                Pair(posts, users)
            }
        }.flatMapCompletable {
            Completable.fromRunnable {
                refreshInTx(it.first, it.second)
            }
        }
    }

    private fun refreshInTx(first: List<PostResource>, second: List<UserResource>) {
        transactionExecutor.execute {
            userDAO.deleteAll()
            postDAO.deleteAll()
            userDAO.insert(second.map {
                User(it.id, it.username)
            })
            postDAO.insert(first.map {
                Post(
                    it.id,
                    it.title,
                    it.body,
                    it.userId
                )
            })
        }
    }
}