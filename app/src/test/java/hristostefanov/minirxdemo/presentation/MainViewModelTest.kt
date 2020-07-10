package hristostefanov.minirxdemo.presentation

import hristostefanov.minirxdemo.WONT_HAPPEN_TIMEOUT_MS
import hristostefanov.minirxdemo.business.interactors.*
import hristostefanov.minirxdemo.testViewModelKeepsStateOfProperty
import hristostefanov.minirxdemo.utilities.StringSupplier
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.*
import org.mockito.Mockito.mock
import java.util.concurrent.TimeUnit

class MainViewModelTest {
    private val autoRefreshService = mock(AutoRefreshService::class.java)
    private val observePostsInteractor = mock(ObservePostsInteractor::class.java)
    private val refreshInteractor = mock(RefreshInteractor::class.java)
    private val observeBackgroundOperationStatus =
        mock(ObserveBackgroundOperationStatus::class.java)

    private val refreshCompletable = spy(Completable.complete())

    private val stringSupplier = object : StringSupplier {
        override fun get(resId: Int): String = "Unknown error"
    }

    private val viewModelUnderTest: MainViewModel by lazy {
        MainViewModel(
            observePostsInteractor,
            refreshInteractor,
            stringSupplier,
            observeBackgroundOperationStatus,
            autoRefreshService
        )
    }

    // test data
    private val post1 = PostSummary("Title", "username")
    val formattedPost1 = FormattedPostSummary("Title", "@username")

    @Before
    fun beforeAll() {
        given(observeBackgroundOperationStatus.status).willReturn(Observable.never())
        given(observePostsInteractor.source).willReturn(Observable.never())
        given(refreshInteractor.execution).willReturn(refreshCompletable)
    }

    @Test
    fun `Starts @AutoRefreshService when initialized`() {
        viewModelUnderTest.init()

        then(autoRefreshService).should().start()
    }

    @Test
    fun `Stops @AutoRefreshService when cleared`() {
        viewModelUnderTest.init()

        viewModelUnderTest.onCleared()

        then(autoRefreshService).should().stop()
    }

    @Test
    fun `Executes @RefreshInteractor on Refresh command`() {
        viewModelUnderTest.init()

        viewModelUnderTest.refreshCommandObserver.onNext(Unit)

        then(refreshCompletable).should().subscribe(hristostefanov.minirxdemo.any<CompletableObserver>())
    }


    @Test
    fun `Routes @ObserveBackgroundOperationStatus Failures to #errorMessage without terminating it`() {
        val errorInfiniteObservable =
            Observable.concat<Status>(Observable.just(Failure("error")), Observable.never())
        given(observeBackgroundOperationStatus.status).willReturn(errorInfiniteObservable)

        val observer = viewModelUnderTest.errorMessage.test()
        viewModelUnderTest.init()

        // expecting default value + payload
        observer.awaitCount(2)
            .assertValueCount(2) // awaitCount may fail with time-out hence the assert
        observer.awaitTerminalEvent(WONT_HAPPEN_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        observer.assertNotTerminated()
    }


    @Test
    fun `Keeps #postList state`() {
        val listPostsObservableSpy =
            spy(Observable.concat(Observable.just(listOf(post1)), Observable.never()))
        given(observePostsInteractor.source).willReturn(listPostsObservableSpy)

        testViewModelKeepsStateOfProperty(
            listPostsObservableSpy,
            viewModelUnderTest.postList,
            viewModelUnderTest::init
        )
    }


    @Test
    fun `Keeps #errorMessage state`() {
        val sourceObservableSpy =
            spy(Observable.concat<Status>(Observable.just(Failure("error")), Observable.never()))
        given(observeBackgroundOperationStatus.status).willReturn(sourceObservableSpy)

        testViewModelKeepsStateOfProperty(
            sourceObservableSpy,
            viewModelUnderTest.errorMessage,
            viewModelUnderTest::init
        )
    }

    @Test
    fun `Routes and formats @ObservePostsInteractor emissions to #postList without terminating it`() {
        val listPostsObservable =
            Observable.concat(Observable.just(listOf(post1)), Observable.never())
        given(observePostsInteractor.source).willReturn(listPostsObservable)

        val observer = viewModelUnderTest.postList.test()
        viewModelUnderTest.init()

        // expecting the default value + payload
        observer.awaitCount(2).assertValueAt(1, listOf(formattedPost1))

        observer.awaitTerminalEvent(WONT_HAPPEN_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        observer.assertNotTerminated()
    }

    @Test
    fun `Routes @RefreshInteractor errors to #errorMessage without terminating it`() {
        val errorCompletable = Completable.error(Throwable("error"))
        given(refreshInteractor.execution).willReturn(errorCompletable)
        val observer = viewModelUnderTest.errorMessage.test()
        viewModelUnderTest.init()

        viewModelUnderTest.refreshCommandObserver.onNext(Unit)

        // wait the default value, one empty string and the payload
        observer.awaitCount(3).assertValueAt(2, "error")

        observer.awaitTerminalEvent(WONT_HAPPEN_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        observer.assertNotTerminated()
    }


    @Test
    fun `Sets #foregroundProgressIndicator when starts executing Refresh command`() {
        val observer = viewModelUnderTest.foregroundProgressIndicator.test()
        viewModelUnderTest.init()

        viewModelUnderTest.refreshCommandObserver.onNext(Unit)

        // await two values - the default and true
        observer.awaitCount(2).assertValueAt(1, true)
    }

    @Test
    fun `Clears #foregroundProgressIndicator when @RefreshInteractor execution completes`() {
        val executionSubject = PublishSubject.create<Unit>()
        given(refreshInteractor.execution).willReturn(
            Completable.fromObservable(
                executionSubject
            )
        )
        val observer = viewModelUnderTest.foregroundProgressIndicator.test()
        viewModelUnderTest.init()
        viewModelUnderTest.refreshCommandObserver.onNext(Unit)
        // await two values - the default and true
        observer.awaitCount(2).assertValueAt(1, true)

        executionSubject.onComplete()

        observer.awaitCount(3).assertValueAt(2, false)
    }


    @Test
    fun `Sets #backgroundProgressIndicator when @ObserveBackgroundOperationStatus reports "InProgress"`() {
        given(observeBackgroundOperationStatus.status).willReturn(
            Observable.concat(
                Observable.just(
                    InProgress
                ), Observable.never()
            )
        )
        val observer = viewModelUnderTest.backgroundProgressIndicator.test()

        viewModelUnderTest.init()

        // await two values - the default and true
        observer.awaitCount(2).assertValueAt(1, true)
    }

    @Test
    fun `Clears #backgroundProgressIndicator when @ObserveBackgroundOperationStatus reports "Success"`() {
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
    fun `Clears #backgroundProgressIndicator when @ObserveBackgroundOperationStatus reports "Failure"`() {
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