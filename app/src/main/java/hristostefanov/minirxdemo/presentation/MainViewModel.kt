package hristostefanov.minirxdemo.presentation

import androidx.lifecycle.ViewModel
import hristostefanov.minirxdemo.R
import hristostefanov.minirxdemo.business.interactors.*
import hristostefanov.minirxdemo.utilities.StringSupplier
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@Suppress("UnstableApiUsage")
class MainViewModel @Inject constructor(
    private val observePosts: ObservePosts,
    private val requestRefreshLocalData: RequestRefreshLocalData,
    private val stringSupplier: StringSupplier,
    private val observeBackgroundOperationStatus: ObserveBackgroundOperationStatus,
    private val autoRefreshLocalDataService: AutoRefreshLocalDataService
) :
    ViewModel() {

    private val _foregroundProgressIndicator = BehaviorSubject.createDefault(false)
    val foregroundProgressIndicator: Observable<Boolean> = _foregroundProgressIndicator

    private val _backgroundProgressIndicator = BehaviorSubject.createDefault(false)
    val backgroundProgressIndicator: Observable<Boolean> = _backgroundProgressIndicator

    private val _postList = BehaviorSubject.createDefault<List<PostSummary>>(emptyList())
    val postList: Observable<List<PostSummary>> = _postList

    private val _errorMessage = BehaviorSubject.createDefault("")
    val errorMessage: Observable<String> = _errorMessage

    val refreshCommandObserver: Observer<Unit> = object : Observer<Unit> {
        override fun onNext(t: Unit) {
            _errorMessage.onNext("")
            _foregroundProgressIndicator.onNext(true)

            // TODO what to do with this disposable? some week reference composite disposable
            requestRefreshLocalData.execution.subscribe({
                _errorMessage.onNext("")
                _foregroundProgressIndicator.onNext(false)
            },{
                _foregroundProgressIndicator.onNext(false)
                val msg = it.message ?: stringSupplier.get(R.string.unknown_error)
                _errorMessage.onNext(msg)
            })
        }

        override fun onComplete() {}

        override fun onSubscribe(d: Disposable) {}

        override fun onError(e: Throwable) {}
    }

    private val compositeDisposable = CompositeDisposable()

    fun init() {
        observeBackgroundOperationStatus.status.subscribe {
                // TODO use a dedicated progress indicator for background ops
                when (it) {
                    is Failure -> {
                        _errorMessage.onNext(it.message)
                        _backgroundProgressIndicator.onNext(false)
                    }
                    is Success -> {
                        _backgroundProgressIndicator.onNext(false)
                        _errorMessage.onNext("")
                    }
                    is InProgress -> {
                        _backgroundProgressIndicator.onNext(true)
                        _errorMessage.onNext("")
                    }
                }
            }.also {
            compositeDisposable.add(it)
        }

        observePosts.source
            .subscribe(
                {
                    _postList.onNext(it)
                }, {
                    throw AssertionError("Infinite stream should not terminate with error", it)
                }, {
                    throw AssertionError("Infinite stream should not complete")
                }
            ).also {
                compositeDisposable.add(it)
            }

        autoRefreshLocalDataService.start()
    }

    public override fun onCleared() {
        autoRefreshLocalDataService.stop()
        // calling #dispose instead of #clear because the container will not be reused
        compositeDisposable.dispose()
        super.onCleared()
    }
}