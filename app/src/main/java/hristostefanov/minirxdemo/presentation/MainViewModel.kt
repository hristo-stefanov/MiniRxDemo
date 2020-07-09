package hristostefanov.minirxdemo.presentation

import androidx.lifecycle.ViewModel
import hristostefanov.minirxdemo.R
import hristostefanov.minirxdemo.business.interactors.*
import hristostefanov.minirxdemo.utilities.StringSupplier
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
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

    private val _postList = BehaviorSubject.createDefault<List<FormattedPostSummary>>(emptyList())
    val postList: Observable<List<FormattedPostSummary>> = _postList

    private val _errorMessage = BehaviorSubject.createDefault("")
    val errorMessage: Observable<String> = _errorMessage

    private val _refreshCommandSubject = PublishSubject.create<Unit>()
    val refreshCommandObserver: Observer<Unit> = _refreshCommandSubject

    private val compositeDisposable = CompositeDisposable()

    fun init() {
        // using concatMap to auto-dispose the mapped completable when this VM is cleared
        _refreshCommandSubject.concatMapCompletable {
            requestRefreshLocalData.execution.doOnSubscribe {
                _errorMessage.onNext("")
                _foregroundProgressIndicator.onNext(true)
            }.doOnError {
                val msg = it.message ?: stringSupplier.get(R.string.unknown_error)
                _errorMessage.onNext(msg)
            }.doFinally {
                _foregroundProgressIndicator.onNext(false)
            }.onErrorComplete() // prevent disposing the source on error
        }.subscribe().also {
            compositeDisposable.add(it)
        }

        observeBackgroundOperationStatus.status.subscribe {
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
                { list ->
                    val formattedList = list.map {
                        FormattedPostSummary(it.title, "@${it.username}")
                    }
                    _postList.onNext(formattedList)
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