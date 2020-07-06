package hristostefanov.minirxdemo

import hristostefanov.minirxdemo.business.interactors.*
import hristostefanov.minirxdemo.presentation.MainViewModel
import hristostefanov.minirxdemo.utilities.StringSupplier
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.*
import org.mockito.Mockito.mock
import java.util.concurrent.TimeUnit

private const val TIMEOUT_MS = 200L

class MainViewModelTest {
    private val autoRefreshLocalDataService = mock(AutoRefreshLocalDataService::class.java)
    private val observePosts = mock(ObservePosts::class.java)
    private val requestRefreshLocalData = mock(RequestRefreshLocalData::class.java)
    private val observeBackgroundOperationStatus =
        mock(ObserveBackgroundOperationStatus::class.java)

    private val requestRefreshCompletable = spy(Completable.complete())

    private val stringSupplier = object : StringSupplier {
        override fun get(resId: Int): String = "Unknown error"
    }

    private val viewModelUnderTest: MainViewModel by lazy {
        MainViewModel(
            observePosts,
            requestRefreshLocalData,
            stringSupplier,
            observeBackgroundOperationStatus,
            autoRefreshLocalDataService
        )
    }

    // test data
    private val post1 = PostFace("Title", "body")

    @Before
    fun beforeAll() {
        given(observeBackgroundOperationStatus.status).willReturn(Observable.never())
        given(observePosts.source).willReturn(Observable.never())
        given(requestRefreshLocalData.execution).willReturn(requestRefreshCompletable)
    }

    @Test
    fun `Starts autoRefreshLocalDataService when initialized`() {
        viewModelUnderTest.init()

        then(autoRefreshLocalDataService).should().start()
    }

    @Test
    fun `Stops autoRefreshLocalDataService when cleared`() {
        viewModelUnderTest.init()

        viewModelUnderTest.onCleared()

        then(autoRefreshLocalDataService).should().stop()
    }

    @Test
    fun `Executes requestRefreshLocalData on Refresh command`() {
        viewModelUnderTest.init()

        viewModelUnderTest.refreshCommandObserver.onNext(Unit)

        then(requestRefreshCompletable).should().subscribe(any<CompletableObserver>())
    }


    @Test
    fun `Routes #observeBackgroundOperationStatus Failures to #errorMessage without terminating it`() {
        val errorInfiniteObservable =
            Observable.concat<Status>(Observable.just(Failure("error")), Observable.never())
        given(observeBackgroundOperationStatus.status).willReturn(errorInfiniteObservable)

        val observer = viewModelUnderTest.errorMessage.test()
        viewModelUnderTest.init()

        // expecting default value + payload
        observer.awaitCount(2)
            .assertValueCount(2) // awaitCount may fail with time-out hence the assert
        observer.awaitTerminalEvent(TIMEOUT_MS, TimeUnit.MILLISECONDS)
        observer.assertNotTerminated()
    }


    /**
     * Rules:
     *
     * When re-subscribing to an output:
     * * must re-emit the last value of the output
     * * must not re-subscribe to the interactor output
     */
    @Test
    fun `MVVM - Re-subscribing postList`() {
        val listPostsObservableSpy =
            spy(Observable.concat(Observable.just(listOf(post1)), Observable.never()))
        given(observePosts.source).willReturn(listPostsObservableSpy)
        val observer1 = viewModelUnderTest.postList.test()
        viewModelUnderTest.init()
        // await the default + new emission, assert the value count cause awaitCount may timeout
        observer1.awaitCount(2).assertValueCount(2)

        // re-subscribe
        observer1.dispose()
        val observer2 = viewModelUnderTest.postList.test()

        observer2
            .awaitCount(1) // getting only the latest emission
            .assertValue(listOf(post1))

        // .subscribe() is called through a decorator returned by .subscribeOn()
        then(listPostsObservableSpy).should(times(1)).subscribe(any<Observer<List<PostFace>>>())
    }

    /**
     * Rules:
     *
     * When re-subscribing to an output:
     * * must re-emit the last value of the output
     * * must not re-execute the interactor
     * * must not re-subscribe to the interactor output
     */
    @Test
    fun `MVVM Re-subscribing errorMessage`() {
        val errorInfiniteObservable =
            spy(Observable.concat<Status>(Observable.just(Failure("error")), Observable.never()))
        given(observeBackgroundOperationStatus.status).willReturn(errorInfiniteObservable)

        val observer1 = viewModelUnderTest.errorMessage.test()
        viewModelUnderTest.init()

        // await the default + new emission, assert the value count cause awaitCount may timeout
        observer1.awaitCount(2).assertValueCount(2)

        // re-subscribe
        observer1.dispose()
        val errorMessageObserver2 = viewModelUnderTest.errorMessage.test()

        errorMessageObserver2
            .awaitCount(1) // getting only the latest emission
            .assertValue("error")

        // .subscribe() is called through a decorator returned by .subscribeOn()
        then(errorInfiniteObservable).should(times(1)).subscribe(any<Observer<Status>>())
    }

    @Test
    fun `Routes #observePosts emissions to #postList without terminating it`() {
        val listPostsObservable =
            Observable.concat(Observable.just(listOf(post1)), Observable.never())
        given(observePosts.source).willReturn(listPostsObservable)

        val observer = viewModelUnderTest.postList.test()
        viewModelUnderTest.init()

        // expecting the default value + payload
        observer.awaitCount(2).assertValueAt(1, listOf(post1))

        observer.awaitTerminalEvent(TIMEOUT_MS, TimeUnit.MILLISECONDS)
        observer.assertNotTerminated()
    }

    @Test
    fun `Routes #requestRefreshLocalData errors to #errorMessage without terminating it`() {
        val errorCompletable = Completable.error(Throwable("error"))
        given(requestRefreshLocalData.execution).willReturn(errorCompletable)
        val observer = viewModelUnderTest.errorMessage.test()
        viewModelUnderTest.init()

        viewModelUnderTest.refreshCommandObserver.onNext(Unit)

        // wait the default value, one empty string and the payload
        observer.awaitCount(3).assertValueAt(2, "error")

        observer.awaitTerminalEvent(TIMEOUT_MS, TimeUnit.MILLISECONDS)
        observer.assertNotTerminated()
    }


    @Test
    fun `Sets foregroundProgressIndicator when starts executing requestRefreshLocalData`() {
        val observer = viewModelUnderTest.foregroundProgressIndicator.test()
        viewModelUnderTest.init()

        viewModelUnderTest.refreshCommandObserver.onNext(Unit)

        // await two values - the default and true
        observer.awaitCount(2).assertValueAt(1,true)
    }

    @Test
    fun `Clears foregroundProgressIndicator when requestRefreshLocalData execution completes`() {
        val executionSubject = PublishSubject.create<Unit>()
        given(requestRefreshLocalData.execution).willReturn(Completable.fromObservable(executionSubject))
        val observer = viewModelUnderTest.foregroundProgressIndicator.test()
        viewModelUnderTest.refreshCommandObserver.onNext(Unit)
        // await two values - the default and true
        observer.awaitCount(2).assertValueAt(1,true)

        executionSubject.onComplete()

        observer.awaitCount(3).assertValueAt(2, false)
    }


    @Test
    fun `Sets backgroundProgressIndicator when observeBackgroundOperationStatus reports InProgress`() {
        given(observeBackgroundOperationStatus.status).willReturn(Observable.concat(Observable.just(InProgress), Observable.never()))
        val observer = viewModelUnderTest.backgroundProgressIndicator.test()

        viewModelUnderTest.init()

        // await two values - the default and true
        observer.awaitCount(2).assertValueAt(1, true)
    }

    @Test
    fun `Clears backgroundProgressIndicator when observeBackgroundOperationStatus reports Success`() {
        val statusSubject = PublishSubject.create<Status>()
        given(observeBackgroundOperationStatus.status).willReturn(statusSubject)
        val observer = viewModelUnderTest.backgroundProgressIndicator.test()
        viewModelUnderTest.init()
        statusSubject.onNext(InProgress)
        // await two values - the default and true
        observer.awaitCount(2).assertValueAt(1, true)

        statusSubject.onNext(Success)

        observer.awaitCount(3).assertValueAt(2, false)
    }

    @Test
    fun `Clears backgroundProgressIndicator when observeBackgroundOperationStatus reports Failure`() {
        val statusSubject = PublishSubject.create<Status>()
        given(observeBackgroundOperationStatus.status).willReturn(statusSubject)
        val observer = viewModelUnderTest.backgroundProgressIndicator.test()
        viewModelUnderTest.init()
        statusSubject.onNext(InProgress)
        // await two values - the default and true
        observer.awaitCount(2).assertValueAt(1, true)

        statusSubject.onNext(Failure("error"))

        observer.awaitCount(3).assertValueAt(2, false)
    }

}