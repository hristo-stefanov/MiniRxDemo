package hristostefanov.minirxdemo.utilities

import hristostefanov.minirxdemo.WONT_HAPPEN_TIMEOUT_MS
import io.reactivex.Completable
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.concurrent.TimeUnit

class CompletableStatusReporterTest {
    private val stringSupplier = mock(StringSupplier::class.java)
    private val unit = CompletableStatusReporter(stringSupplier)


    @Test
    fun `Reports Failure on Error without terminating`() {
        val completable = Completable.error(Exception("error"))
        val observer = unit.status.test()

        unit.tapInto(completable)
            .onErrorComplete()  // prevents logging the error
            .subscribe()

        // the first value is InProgress
        observer.awaitCount(2).assertValueAt(1,
            Failure("error")
        )
        observer.awaitTerminalEvent(WONT_HAPPEN_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        observer.assertNotTerminated()
    }

    @Test
    fun `Reports Success on Complete without terminating`() {
        val completable = Completable.complete()
        val observer = unit.status.test()

        unit.tapInto(completable).subscribe()

        // the first value is InProgress
        observer.awaitCount(2).assertValueAt(1,
            Success
        )
        observer.awaitTerminalEvent(WONT_HAPPEN_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        observer.assertNotTerminated()
    }

    @Test
    fun `Reports InProgress on Subscribe without terminating`() {
        val completable = Completable.never()

        val observer = unit.status.test()

        unit.tapInto(completable).subscribe()

        observer.awaitCount(1).assertValueAt(0,
            InProgress
        )
        observer.awaitTerminalEvent(WONT_HAPPEN_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        observer.assertNotTerminated()
    }

    // TODO test with exception without message
}
