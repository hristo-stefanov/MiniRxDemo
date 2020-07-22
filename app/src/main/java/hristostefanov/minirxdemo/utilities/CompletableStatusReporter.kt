package hristostefanov.minirxdemo.utilities

import hristostefanov.minirxdemo.R
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class CompletableStatusReporter @Inject constructor(
    private val stringSupplier: StringSupplier
) {
    private val _statusSubject = BehaviorSubject.create<Status>()
    val status: Observable<Status> = _statusSubject

    fun tapInto(completable: Completable): Completable {
        return completable
            .doOnError {
                val msg = it.message ?: stringSupplier.get(R.string.unknown_error)
                _statusSubject.onNext(
                    Failure(
                        msg
                    )
                )
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