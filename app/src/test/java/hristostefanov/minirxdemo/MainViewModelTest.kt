package hristostefanov.minirxdemo

import hristostefanov.minirxdemo.business.interactors.*
import hristostefanov.minirxdemo.presentation.MainViewModel
import hristostefanov.minirxdemo.utilities.StringSupplier
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.Observable
import io.reactivex.Observer
import junit.framework.Assert.fail
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
    val viewModelUnderTest: MainViewModel by lazy {
        MainViewModel(
            observePosts,
            requestRefreshLocalData,
            stringSupplier,
            observeBackgroundOperationStatus,
            autoRefreshLocalDataService
        )
    }

    // test data
    val post1 = PostFace("Title", "body")

    @Before
    fun beforeAll() {
        given(observeBackgroundOperationStatus.status).willReturn(Observable.never())
        given(observePosts.source).willReturn(Observable.never())
        given(requestRefreshLocalData.execution).willReturn(requestRefreshCompletable)
        // TODO observePosts?
    }

    @Test
    fun `Starts autoRefreshLocalDataService when initialized`() {
        viewModelUnderTest.init()
        then(autoRefreshLocalDataService).should().start()
    }

    @Test
    fun `Stops autoRefreshLocalDataService when cleared`() {
        fail()
    }

    @Test
    fun `Executes requestRefreshLocalData on Refresh command`() {
        viewModelUnderTest.init()

        viewModelUnderTest.refreshCommandObserver.onNext(Unit)

        then(requestRefreshLocalData).should().execution
        then(requestRefreshLocalData).shouldHaveNoMoreInteractions()
        then(requestRefreshCompletable).should().subscribe(any<CompletableObserver>())
    }

    /**
     * Rule: observable output properties are infinite
     */
    @Test
    fun `MVVM - WHEN observePosts emits THEN will not complete`() {
        val listPostsObservable =
            Observable.concat(Observable.just(listOf(post1)), Observable.never())
        given(observePosts.source).willReturn(listPostsObservable)

        val observer = viewModelUnderTest.postList.test()
        viewModelUnderTest.init()

        // expecting default value + payload
        observer.awaitCount(2)
        // await onComplete or onError, but expected to just time out
        observer.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)
        observer.assertNotTerminated()
    }

    /**
     * Rule: observable output properties are infinite
     */
    @Test
    fun `MVVM - WHEN errorMessage emits THEN will not complete`() {
        val errorInfiniteObservable =
            Observable.concat<Status>(Observable.just(Failure("error")), Observable.never())
        given(observeBackgroundOperationStatus.status).willReturn(errorInfiniteObservable)

        val observer = viewModelUnderTest.errorMessage.test()
        viewModelUnderTest.init()

        // expecting default value + payload
        observer.awaitCount(2)
            .assertValueCount(2) // awaitCount may fail with time-out hence the assert
        observer.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)
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


    /**
     * Rule: map [Post] to [PostFace]
     */
    @Test
    fun `Routes observePosts emissions to postList`() {
        val listPostsObservable =
            Observable.concat(Observable.just(listOf(post1)), Observable.never())
        given(observePosts.source).willReturn(listPostsObservable)

        val postListObserver = viewModelUnderTest.postList.test()
        viewModelUnderTest.init()

        // expecting the default value + payload
        postListObserver.awaitCount(2).assertValueAt(1, listOf(post1))
    }

    /**
     *  Rule: map [Throwable] to [String]
     */
    @Test
    fun `Routes requestRefreshLocalData errors to errorMessage`() {
        val errorCompletable = Completable.error(Throwable("error"))
        given(requestRefreshLocalData.execution).willReturn(errorCompletable)

        val observer = viewModelUnderTest.errorMessage.test()
        viewModelUnderTest.init()
        viewModelUnderTest.refreshCommandObserver.onNext(Unit)

        // TODO too much details about internal workings here
        // expecting the default value + one empty string + payload
        observer.awaitCount(3).assertValueCount(3)
        observer.assertValueAt(2, "error")
    }

    @Test
    fun `Keeps foregroundProgressIndicator set while requestRefreshLocalData is executing`() {
        // TODO implement
        fail()
    }

    @Test
    fun `Keeps backgroundProgressIndicator set while observeBackgroundOperationStatus reports InProgress`() {
        // TODO implement
        fail()
    }

}