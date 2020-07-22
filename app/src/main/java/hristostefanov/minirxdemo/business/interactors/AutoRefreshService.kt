package hristostefanov.minirxdemo.business.interactors

import hristostefanov.minirxdemo.utilities.Status
import hristostefanov.minirxdemo.utilities.CompletableStatusReporter
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class AutoRefreshService @Inject constructor(
    private val refreshInteractor: RefreshInteractor,
    @Named("autoRefreshIntervalMillis")
    private val intervalMillis: Long,
    private val completableStatusReporter: CompletableStatusReporter
) {
    private var disposable: Disposable? = null

    val status: Observable<Status> = completableStatusReporter.status

    fun start() {
        if (disposable != null)
            return
        disposable = Observable.interval(0, intervalMillis, TimeUnit.MILLISECONDS).concatMapCompletable {
            completableStatusReporter.tapInto(refreshInteractor.execution)
                .onErrorComplete() // prevent disposing the source on error
        }.subscribe()
    }

    fun stop() {
        disposable?.dispose().also {
            disposable = null
        }
    }
}
