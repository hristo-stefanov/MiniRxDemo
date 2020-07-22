package hristostefanov.minirxdemo.presentation

import hristostefanov.minirxdemo.WONT_HAPPEN_TIMEOUT_MS
import hristostefanov.minirxdemo.business.interactors.AutoRefreshService
import hristostefanov.minirxdemo.business.interactors.ObservePostsInteractor
import hristostefanov.minirxdemo.business.interactors.PostSummary
import hristostefanov.minirxdemo.business.interactors.RefreshInteractor
import hristostefanov.minirxdemo.testViewModelKeepsStateOfProperty
import hristostefanov.minirxdemo.utilities.*
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

    private val refreshCompletable = spy(Completable.complete())

    private val stringSupplier = object : StringSupplier {
        override fun get(resId: Int): String = "Unknown error"
    }

    private val viewModelUnderTest: MainViewModel by lazy {
        MainViewModel(
            observePostsInteractor,
            refreshInteractor,
            stringSupplier,
            autoRefreshService
        )
    }

    // test data
    private val post1 = PostSummary("Title", "username")
    private val formattedPost1 = FormattedPostSummary("Title", "@username")

    @Before
    fun beforeAll() {
        given(autoRefreshService.status).willReturn(Observable.never())
        given(observePostsInteractor.source).willReturn(Observable.never())
        given(refreshInteractor.execution).willReturn(refreshCompletable)
    }

    @Test
    fun `Starts auto-refreshing local data when initialized`() {
        viewModelUnderTest.init()

        then(autoRefreshService).should().start()
    }

    @Test
    fun `Stops auto-refreshing local data when cleared`() {
        viewModelUnderTest.init()

        viewModelUnderTest.onCleared()

        then(autoRefreshService).should().stop()
    }

    @Test
    fun `Executes Refresh command when requested`() {
        viewModelUnderTest.init()

        viewModelUnderTest.refreshCommandObserver.onNext(Unit)

        then(refreshCompletable).should().subscribe(hristostefanov.minirxdemo.any<CompletableObserver>())
    }


    @Test
    fun `Displays error message on each auto-refresh service failure`() {
        val errorInfiniteObservable =
            Observable.concat<Status>(Observable.just(
                Failure("error")
            ), Observable.never())
        given(autoRefreshService.status).willReturn(errorInfiniteObservable)

        val observer = viewModelUnderTest.errorMessage.test()
        viewModelUnderTest.init()

        // expecting default value + payload
        observer.awaitCount(2)
            .assertValueCount(2) // awaitCount may fail with time-out hence the assert
        // not terminated
        observer.awaitTerminalEvent(WONT_HAPPEN_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        observer.assertNotTerminated()
    }


    @Test
    fun `Keeps post list state`() {
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
    fun `Keeps error message state`() {
        val sourceObservableSpy =
            spy(Observable.concat<Status>(Observable.just(
                Failure("error")
            ), Observable.never()))
        given(autoRefreshService.status).willReturn(sourceObservableSpy)

        testViewModelKeepsStateOfProperty(
            sourceObservableSpy,
            viewModelUnderTest.errorMessage,
            viewModelUnderTest::init
        )
    }


    @Test
    fun `Displays each post list emission`() {
        val listPostsObservable =
            Observable.concat(Observable.just(listOf(post1)), Observable.never())
        given(observePostsInteractor.source).willReturn(listPostsObservable)

        val observer = viewModelUnderTest.postList.test()
        viewModelUnderTest.init()

        // expecting the default value + payload
        observer.awaitCount(2).assertValueAt(1, listOf(formattedPost1))
        // not terminated
        observer.awaitTerminalEvent(WONT_HAPPEN_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        observer.assertNotTerminated()
    }

    @Test
    fun `Displays error message on evey Refresh command error`() {
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
    fun `Sets refresh indicator when starts executing Refresh command`() {
        val observer = viewModelUnderTest.refreshIndicator.test()
        viewModelUnderTest.init()

        viewModelUnderTest.refreshCommandObserver.onNext(Unit)

        // await two values - the default and true
        observer.awaitCount(2).assertValueAt(1, true)
    }

    @Test
    fun `Clears refresh indicator when Refresh command execution completes`() {
        val executionSubject = PublishSubject.create<Unit>()
        given(refreshInteractor.execution).willReturn(
            Completable.fromObservable(
                executionSubject
            )
        )
        val observer = viewModelUnderTest.refreshIndicator.test()
        viewModelUnderTest.init()
        viewModelUnderTest.refreshCommandObserver.onNext(Unit)
        // await two values - the default and true
        observer.awaitCount(2).assertValueAt(1, true)

        executionSubject.onComplete()

        observer.awaitCount(3).assertValueAt(2, false)
    }


    @Test
    fun `Sets progress indicator when auto-refresh service reports in progress`() {
        given(autoRefreshService.status).willReturn(
            Observable.concat(
                Observable.just(
                    InProgress
                ), Observable.never()
            )
        )
        val observer = viewModelUnderTest.progressIndicator.test()

        viewModelUnderTest.init()

        // await two values - the default and true
        observer.awaitCount(2).assertValueAt(1, true)
    }

    @Test
    fun `Clears progress indicator when auto-refresh service reports success`() {
        val statusSubject = PublishSubject.create<Status>()
        given(autoRefreshService.status).willReturn(statusSubject)
        val observer = viewModelUnderTest.progressIndicator.test()
        viewModelUnderTest.init()
        statusSubject.onNext(InProgress)
        // await two values - the default and true
        observer.awaitCount(2).assertValueAt(1, true)

        statusSubject.onNext(Success)

        observer.awaitCount(3).assertValueAt(2, false)
    }

    @Test
    fun `Clears progress indicator when auto-refresh service reports failure`() {
        val statusSubject = PublishSubject.create<Status>()
        given(autoRefreshService.status).willReturn(statusSubject)
        val observer = viewModelUnderTest.progressIndicator.test()
        viewModelUnderTest.init()
        statusSubject.onNext(InProgress)
        // await two values - the default and true
        observer.awaitCount(2).assertValueAt(1, true)

        statusSubject.onNext(Failure("error"))

        observer.awaitCount(3).assertValueAt(2, false)
    }

}