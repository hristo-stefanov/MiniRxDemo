package hristostefanov.minirxdemo.presentation

import androidx.lifecycle.ViewModel
import hristostefanov.minirxdemo.R
import hristostefanov.minirxdemo.business.interactors.Queries
import hristostefanov.minirxdemo.business.interactors.PostFace
import hristostefanov.minirxdemo.business.interactors.Commands
import hristostefanov.minirxdemo.utilities.StringSupplier
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@Suppress("UnstableApiUsage")
class MainViewModel @Inject constructor(
    private val queries: Queries,
    private val commands: Commands,
    private val stringSupplier: StringSupplier
) :
    ViewModel() {

    private val _progressIndicator = BehaviorSubject.createDefault(false)
    val progressIndicator: Observable<Boolean> = _progressIndicator

    private val _postList = BehaviorSubject.createDefault<List<PostFace>>(emptyList())
    val postList: Observable<List<PostFace>> = _postList

    private val _errorMessage = BehaviorSubject.createDefault("")
    val errorMessage: Observable<String> = _errorMessage

    // the set of Observables that can be observed is dynamic hence a Subject is used
    private val _refreshSubject = PublishSubject.create<Unit>()
    val refreshObserver: Observer<Unit> = _refreshSubject

    val compositeDisposable = CompositeDisposable()

    fun init() {
        // infinite observable
        val refreshTrigger = Observable.concat(Observable.just(Unit), _refreshSubject)

        // using concatMap simplifies disposal of chained streams
        refreshTrigger.concatMapCompletable {
            commands.refresh
                .subscribeOn(Schedulers.io())
                .doOnError {
                    val msg = it.message ?: stringSupplier.get(R.string.unknown_error)
                    _errorMessage.onNext(msg)
                }
                .doOnComplete {
                    _errorMessage.onNext("")
                }
                .doOnSubscribe {
                    _progressIndicator.onNext(true)
                }
                .doFinally {
                    _progressIndicator.onNext(false)
                }
                // prevent disposing the trigger
                .onErrorComplete()
        }.subscribe().also {
            compositeDisposable.add(it)
        }


        queries.listTenFirstPosts
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