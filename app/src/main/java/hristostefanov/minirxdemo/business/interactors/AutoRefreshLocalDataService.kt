package hristostefanov.minirxdemo.business.interactors

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AutoRefreshLocalDataService @Inject constructor(
    private val observeBackgroundOperationStatus: ObserveBackgroundOperationStatus,
    private val requestRefreshLocalData: RequestRefreshLocalData
) {
    private var disposable: Disposable? = null

    fun start() {
        if (disposable != null)
            return
        disposable = Observable.interval(1, TimeUnit.HOURS).concatMapCompletable {
            observeBackgroundOperationStatus.observeStatus(requestRefreshLocalData.execution)
                .onErrorComplete() // prevent disposing the source on error
        }.subscribe()
    }

    fun stop() {
        disposable?.dispose().also {
            disposable = null
        }
    }
}