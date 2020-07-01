package hristostefanov.minirxdemo.business.interactors

import hristostefanov.minirxdemo.R
import hristostefanov.minirxdemo.business.entities.Post
import hristostefanov.minirxdemo.business.entities.User
import hristostefanov.minirxdemo.business.gateways.local.PostDAO
import hristostefanov.minirxdemo.business.gateways.local.UserDAO
import hristostefanov.minirxdemo.business.gateways.remote.PostResource
import hristostefanov.minirxdemo.business.gateways.remote.Service
import hristostefanov.minirxdemo.business.gateways.remote.UserResource
import hristostefanov.minirxdemo.utilities.StringSupplier
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Named

class RefreshLocalData @Inject constructor(
    private val service: Service,
    private val userDAO: UserDAO,
    private val postDAO: PostDAO,
    @Named("transactionExecutor")
    private val transactionExecutor: Executor,
    private val stringSupplier: StringSupplier
) {

    private val compositeDisposable = CompositeDisposable()

    private val _refreshSubject = PublishSubject.create<Unit>()
    val refreshObserver: Observer<Unit> = _refreshSubject

    private val _statusSubject = PublishSubject.create<Status>()
    val statusSubject: Observable<Status> = _statusSubject


    // TODO call from App
    fun start() {
        // infinite observable
        val refreshTrigger = Observable.concat(Observable.just(Unit), _refreshSubject)

        // using concatMap simplifies disposal of chained streams
        refreshTrigger.concatMapCompletable {
            execution
                .subscribeOn(Schedulers.io())
                .doOnError {
                    val msg = it.message ?: stringSupplier.get(R.string.unknown_error)
                    _statusSubject.onNext(Failure(msg))
                }
                .doOnComplete {
                    _statusSubject.onNext(Success)
                }
                .doOnSubscribe {
                    _statusSubject.onNext(InProgress)
                }
                .doFinally {
                    // empty
                }
                // prevent disposing the trigger
                .onErrorComplete()
        }.subscribe().also {
            compositeDisposable.add(it)
        }
    }

    // TODO call from App
    fun exit() {
        // using #dispose() cause will not be reused
        compositeDisposable.dispose()
    }

    private val execution: Completable by lazy {
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