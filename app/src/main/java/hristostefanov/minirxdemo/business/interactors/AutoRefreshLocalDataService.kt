package hristostefanov.minirxdemo.business.interactors

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class AutoRefreshLocalDataService @Inject constructor(
    private val observeBackgroundOperationStatus: ObserveBackgroundOperationStatus,
    private val requestRefreshLocalData: RequestRefreshLocalData,
    @Named("autoRefreshIntervalMillis")
    private val intervalMillis: Long
) {
    private var disposable: Disposable? = null

    fun start() {
        if (disposable != null)
            return
        disposable = Observable.interval(0, intervalMillis, TimeUnit.MILLISECONDS).concatMapCompletable {
            observeBackgroundOperationStatus.tapInto(requestRefreshLocalData.execution)
                .onErrorComplete() // prevent disposing the source on error
        }.subscribe()
    }

    fun stop() {
        disposable?.dispose().also {
            disposable = null
        }
    }
}