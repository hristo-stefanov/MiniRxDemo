package hristostefanov.minirxdemo.presentation

import androidx.lifecycle.ViewModel
import hristostefanov.minirxdemo.R
import hristostefanov.minirxdemo.business.interactors.*
import hristostefanov.minirxdemo.utilities.Failure
import hristostefanov.minirxdemo.utilities.InProgress
import hristostefanov.minirxdemo.utilities.StringSupplier
import hristostefanov.minirxdemo.utilities.Success
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@Suppress("UnstableApiUsage")
class MainViewModel @Inject constructor(
    private val observePostsInteractor: ObservePostsInteractor,
    private val refreshInteractor: RefreshInteractor,
    private val stringSupplier: StringSupplier,
    private val autoRefreshService: AutoRefreshService
) :
    ViewModel() {

    private val _refreshIndicator = BehaviorSubject.createDefault(false)
    val refreshIndicator: Observable<Boolean> = _refreshIndicator

    private val _progressIndicator = BehaviorSubject.createDefault(false)
    val progressIndicator: Observable<Boolean> = _progressIndicator

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
            refreshInteractor.execution.doOnSubscribe {
                _errorMessage.onNext("")
                _refreshIndicator.onNext(true)
            }.doOnError {
                val msg = it.message ?: stringSupplier.get(R.string.unknown_error)
                _errorMessage.onNext(msg)
            }.doFinally {
                _refreshIndicator.onNext(false)
            }.onErrorComplete() // prevent disposing the source on error
        }.subscribe().also {
            compositeDisposable.add(it)
        }

        autoRefreshService.status.subscribe {
            when (it) {
                is Failure -> {
                    _errorMessage.onNext(it.message)
                    _progressIndicator.onNext(false)
                }
                is Success -> {
                    _progressIndicator.onNext(false)
                    _errorMessage.onNext("")
                }
                is InProgress -> {
                    _progressIndicator.onNext(true)
                    _errorMessage.onNext("")
                }
            }
        }.also {
            compositeDisposable.add(it)
        }

        observePostsInteractor.source
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

        autoRefreshService.start()
    }

    public override fun onCleared() {
        autoRefreshService.stop()
        // calling #dispose instead of #clear because the container will not be reused
        compositeDisposable.dispose()
        super.onCleared()
    }
}