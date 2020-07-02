package hristostefanov.minirxdemo.business.interactors

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class Startup @Inject constructor(
    private val observeBackgroundOperationStatus: ObserveBackgroundOperationStatus,
    private val refreshLocalData: RefreshLocalData
) {
    private val compositeDisposable = CompositeDisposable()

    // TODO call from App
    fun execute() {
        observeBackgroundOperationStatus.observeStatus(refreshLocalData.execution)
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