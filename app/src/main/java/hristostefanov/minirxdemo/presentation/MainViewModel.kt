package hristostefanov.minirxdemo.presentation

import androidx.lifecycle.ViewModel
import hristostefanov.minirxdemo.R
import hristostefanov.minirxdemo.business.interactors.*
import hristostefanov.minirxdemo.utilities.StringSupplier
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@Suppress("UnstableApiUsage")
class MainViewModel @Inject constructor(
    private val observeTenFirstPosts: ObserveTenFirstPosts,
    private val refreshLocalDataFromRemoteService: RefreshLocalDataFromRemoteService,
    private val stringSupplier: StringSupplier
) :
    ViewModel() {

    private val _progressIndicator = BehaviorSubject.createDefault(false)
    val progressIndicator: Observable<Boolean> = _progressIndicator

    private val _postList = BehaviorSubject.createDefault<List<PostFace>>(emptyList())
    val postList: Observable<List<PostFace>> = _postList

    private val _errorMessage = BehaviorSubject.createDefault("")
    val errorMessage: Observable<String> = _errorMessage

    val refreshObserver: Observer<Unit> = refreshLocalDataFromRemoteService.refreshObserver

    val compositeDisposable = CompositeDisposable()

    fun init() {
        refreshLocalDataFromRemoteService.statusSubject.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
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

        // TODO call from App
        refreshLocalDataFromRemoteService.start()
    }

    override fun onCleared() {
        // calling #dispose instead of #clear because the container will not be reused
        compositeDisposable.dispose()
        super.onCleared()
    }
}