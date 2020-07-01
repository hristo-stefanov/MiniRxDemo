package hristostefanov.minirxdemo.presentation

import androidx.lifecycle.ViewModel
import hristostefanov.minirxdemo.R
import hristostefanov.minirxdemo.business.interactors.*
import hristostefanov.minirxdemo.utilities.StringSupplier
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@Suppress("UnstableApiUsage")
class MainViewModel @Inject constructor(
    private val observeTenFirstPosts: ObserveTenFirstPosts,
    private val refreshLocalData: RefreshLocalData,
    private val stringSupplier: StringSupplier,
    private val observeBackgroundOperationStatus: ObserveBackgroundOperationStatus
) :
    ViewModel() {

    private val _progressIndicator = BehaviorSubject.createDefault(false)
    val progressIndicator: Observable<Boolean> = _progressIndicator

    private val _postList = BehaviorSubject.createDefault<List<PostFace>>(emptyList())
    val postList: Observable<List<PostFace>> = _postList

    private val _errorMessage = BehaviorSubject.createDefault("")
    val errorMessage: Observable<String> = _errorMessage

    val refreshObserver: Observer<Unit> = object : Observer<Unit> {
        override fun onNext(t: Unit) {
            // TODO use dedicated progress indicator
            _errorMessage.onNext("")
            _progressIndicator.onNext(true)
            refreshLocalData.execution.subscribe({
                _errorMessage.onNext("")
                _progressIndicator.onNext(false)
            },{
                _progressIndicator.onNext(false)
                val msg = it.message ?: stringSupplier.get(R.string.unknown_error)
                _errorMessage.onNext(msg)
            })


            /*refreshLocalData.execute()
                .doOnError {
                    val msg = it.message ?: stringSupplier.get(R.string.unknown_error)
                    _errorMessage.onNext(msg)
                }
                .doOnComplete {
                    _errorMessage.onNext("")
                }
                .doOnSubscribe {
                    _errorMessage.onNext("")
                    _progressIndicator.onNext(true)
                }
                .doFinally {
                    _progressIndicator.onNext(false)
                }
                // prevent disposing the source
                .onErrorComplete()
                .also {
                    // TODO
                }*/
        }

        override fun onComplete() {}

        override fun onSubscribe(d: Disposable) {}


        override fun onError(e: Throwable) {}
    }

    val compositeDisposable = CompositeDisposable()

    fun init() {
        observeBackgroundOperationStatus.statusSubject.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                // TODO use a dedicated progress indicator for background ops
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

        observeTenFirstPosts.source
            .subscribeOn(Schedulers.io())
            .subscribe(
                {
                    _postList.onNext(it)
                }, {
                    // this is an observable query which returns infinite stream so it is not supposed
                    // to emit terminal events like error, but we handle it because it could be some
                    // db related error
                    _errorMessage.onNext(it.message ?: stringSupplier.get(R.string.unknown_error))
                }, {
                    throw AssertionError("Should not complete")
                }
            ).also {
                compositeDisposable.add(it)
            }
    }

    override fun onCleared() {
        // calling #dispose instead of #clear because the container will not be reused
        compositeDisposable.dispose()
        super.onCleared()
    }
}