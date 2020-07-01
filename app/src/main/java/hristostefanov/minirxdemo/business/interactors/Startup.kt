package hristostefanov.minirxdemo.business.interactors

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class Startup @Inject constructor(
    private val refreshTxScript: RefreshTxScript,
    private val observeBackgroundOperationStatus: ObserveBackgroundOperationStatus
) {
    private val compositeDisposable = CompositeDisposable()

    // TODO call from App
    fun execute() {
        observeBackgroundOperationStatus.observeStatus(refreshTxScript.execution)
            .subscribeOn(Schedulers.io())
            .subscribe()
            .also {
                compositeDisposable.add(it)
            }
    }

    // TODO call from App
    fun exit() {
        // using #dispose() cause will not be reused
        compositeDisposable.dispose()
    }
}