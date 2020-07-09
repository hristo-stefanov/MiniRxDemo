package hristostefanov.minirxdemo.business.interactors

import hristostefanov.minirxdemo.WONT_HAPPEN_TIMEOUT_MS
import hristostefanov.minirxdemo.utilities.StringSupplier
import io.reactivex.Completable
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.concurrent.TimeUnit

class ObserveBackgroundOperationStatusTest {
    private val stringSupplier = mock(StringSupplier::class.java)
    private val unit = ObserveBackgroundOperationStatus(stringSupplier)

    @Test
    fun `Reports Failure on Error without terminating`() {
        val completable = Completable.error(Exception("error"))
        val observer = unit.status.test()

        unit.observeStatus(completable).subscribe()

        // the first value is InProgress
        observer.awaitCount(2).assertValueAt(1, Failure("error"))
        observer.awaitTerminalEvent(WONT_HAPPEN_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        observer.assertNotTerminated()
    }

    @Test
    fun `Reports Success on Complete without terminating`() {
        val completable = Completable.complete()
        val observer = unit.status.test()

        unit.observeStatus(completable).subscribe()

        // the first value is InProgress
        observer.awaitCount(2).assertValueAt(1, Success)
        observer.awaitTerminalEvent(WONT_HAPPEN_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        observer.assertNotTerminated()
    }

    @Test
    fun `Reports InProgress on Subscribe without terminating`() {
        val completable = Completable.never()

        val observer = unit.status.test()

        unit.observeStatus(completable).subscribe()

        observer.awaitCount(1).assertValueAt(0, InProgress)
        observer.awaitTerminalEvent(WONT_HAPPEN_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        observer.assertNotTerminated()
    }
}