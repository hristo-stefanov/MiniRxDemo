package hristostefanov.minirxdemo.business.interactors

import hristostefanov.minirxdemo.THREAD_SWITCH_TIMEOUT_MS
import hristostefanov.minirxdemo.WONT_HAPPEN_TIMEOUT_MS
import hristostefanov.minirxdemo.any
import hristostefanov.minirxdemo.utilities.StringSupplier
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.*
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit

private const val INTERVAL_MILLIS = THREAD_SWITCH_TIMEOUT_MS * 3

class AutoRefreshServiceTest {
    private val completable = spy(Completable.complete())
    private val stringSupplier = object : StringSupplier {
        override fun get(resId: Int): String = ""
    }

    private val refreshInteractor = mock(RefreshInteractor::class.java)

    private val unit =
        AutoRefreshService(
            refreshInteractor,
            INTERVAL_MILLIS,
            stringSupplier
        )

    @Before
    fun beforeEach() {
        given(refreshInteractor.execution).willReturn(completable)
    }

    @Test
    fun `Executes Refresh when started`() {
        unit.start()

        then(completable).should(timeout(THREAD_SWITCH_TIMEOUT_MS))
            .subscribe(any<CompletableObserver>())
    }

    @Test
    fun `Executes Refresh periodically`() {
        unit.start()

        sleep(INTERVAL_MILLIS)
        then(completable).should(timeout(THREAD_SWITCH_TIMEOUT_MS).times(2))
            .subscribe(any<CompletableObserver>())

        sleep(INTERVAL_MILLIS)
        then(completable).should(timeout(THREAD_SWITCH_TIMEOUT_MS).times(3))
            .subscribe(any<CompletableObserver>())
    }

    @Test
    fun `Reports Failure on Error without terminating`() {
        val completable = Completable.error(Exception("error"))
        val observer = unit.status.test()

        unit.tapInto(completable)
            .onErrorComplete()  // prevents logging the error
            .subscribe()

        // the first value is InProgress
        observer.awaitCount(2).assertValueAt(1, Failure("error"))
        observer.awaitTerminalEvent(WONT_HAPPEN_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        observer.assertNotTerminated()
    }

    @Test
    fun `Reports Success on Complete without terminating`() {
        val completable = Completable.complete()
        val observer = unit.status.test()

        unit.tapInto(completable).subscribe()

        // the first value is InProgress
        observer.awaitCount(2).assertValueAt(1, Success)
        observer.awaitTerminalEvent(WONT_HAPPEN_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        observer.assertNotTerminated()
    }

    @Test
    fun `Reports InProgress on Subscribe without terminating`() {
        val completable = Completable.never()

        val observer = unit.status.test()

        unit.tapInto(completable).subscribe()

        observer.awaitCount(1).assertValueAt(0, InProgress)
        observer.awaitTerminalEvent(WONT_HAPPEN_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        observer.assertNotTerminated()
    }
}