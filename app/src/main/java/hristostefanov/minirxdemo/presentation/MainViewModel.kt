package hristostefanov.minirxdemo.presentation

import androidx.lifecycle.ViewModel
import hristostefanov.minirxdemo.R
import hristostefanov.minirxdemo.business.ListTenFirstPostsInteractor
import hristostefanov.minirxdemo.business.PostInfo
import hristostefanov.minirxdemo.util.Either
import hristostefanov.minirxdemo.util.StringSupplier
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@Suppress("UnstableApiUsage")
class MainViewModel @Inject constructor(
    private val listTenFirstPostsInteractor: ListTenFirstPostsInteractor,
    stringSupplier: StringSupplier
) :
    ViewModel() {

    val postList: Observable<List<PostFace>>
    val errorMessage: Observable<String>

    // the set of Observables that can be observed is dynamic hence a Subject is used
    private val _refreshSubject = PublishSubject.create<Unit>()
    val refreshObserver: Observer<Unit> = _refreshSubject

    init {
        // infinite observable
        val trigger = Observable.merge(_refreshSubject, Observable.just(Object()))

        trigger.flatMapSingle {
            listTenFirstPostsInteractor.query()
                .subscribeOn(Schedulers.io())
                // wrap both data and error emissions in data emissions of Either to prevent
                // the error emission from terminating the Observable returned by flatMap
                .map<Either<String, List<PostInfo>>> {
                    Either.Right(it.toList())
                }.onErrorReturn {
                    Either.Left(it.message ?: stringSupplier.get(R.string.unknown_error))
                }
        }.replay(1).autoConnect(0).also { notification ->
            postList = notification.map {
                if (it is Either.Right) {
                    it.value.map { postInfo -> PostFace(postInfo.title, "@${postInfo.username}") }
                } else {
                    emptyList<PostFace>()
                }
            }

            errorMessage = notification.map {
                if (it is Either.Left) {
                    it.value
                } else {
                    ""
                }
            }
        }
    }
}