package hristostefanov.minirxdemo.business.interactors

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class RefreshLocalData @Inject constructor(
    private val refreshTxScript: RefreshTxScript,
    private val observeBackgroundOperationStatus: ObserveBackgroundOperationStatus
) {
    val execution: Completable = refreshTxScript.execution.subscribeOn(Schedulers.io())
}