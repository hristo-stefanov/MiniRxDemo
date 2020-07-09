package hristostefanov.minirxdemo.business.interactors

import hristostefanov.minirxdemo.R
import hristostefanov.minirxdemo.utilities.StringSupplier
import hristostefanov.minirxdemo.utilities.di.ApplicationScope
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

// TODO class name differs from the file
@ApplicationScope
class ObserveBackgroundOperationStatus @Inject  constructor(
    private val stringSupplier: StringSupplier
) {
    private val _statusSubject = BehaviorSubject.create<Status>()
    val status: Observable<Status> = _statusSubject

    // TODO rename to tapInto
    fun observeStatus(completable: Completable): Completable {
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