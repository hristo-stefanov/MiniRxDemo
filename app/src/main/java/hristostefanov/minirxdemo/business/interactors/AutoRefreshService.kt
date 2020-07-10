package hristostefanov.minirxdemo.business.interactors

import hristostefanov.minirxdemo.R
import hristostefanov.minirxdemo.utilities.StringSupplier
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class AutoRefreshService @Inject constructor(
    private val refreshInteractor: RefreshInteractor,
    @Named("autoRefreshIntervalMillis")
    private val intervalMillis: Long,
    private val stringSupplier: StringSupplier
) {
    private var disposable: Disposable? = null

    private val _statusSubject = BehaviorSubject.create<Status>()
    val status: Observable<Status> = _statusSubject

    fun start() {
        if (disposable != null)
            return
        disposable = Observable.interval(0, intervalMillis, TimeUnit.MILLISECONDS).concatMapCompletable {
            tapInto(refreshInteractor.execution)
                .onErrorComplete() // prevent disposing the source on error
        }.subscribe()
    }

    fun stop() {
        disposable?.dispose().also {
            disposable = null
        }
    }

    // TODO how to hide it?
    fun tapInto(completable: Completable): Completable {
        return completable
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
    }
}

sealed class Status
object InProgress : Status()
data class Failure(val message: String): Status()
object Success: Status()