package hristostefanov.minirxdemo.business.interactors

import hristostefanov.minirxdemo.THREAD_SWITCH_TIMEOUT_MS
import hristostefanov.minirxdemo.any
import hristostefanov.minirxdemo.utilities.CompletableStatusReporter
import hristostefanov.minirxdemo.utilities.StringSupplier
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.*
import java.lang.Thread.sleep

private const val INTERVAL_MILLIS = THREAD_SWITCH_TIMEOUT_MS * 3

class AutoRefreshServiceTest {
    private val executionCompletable = spy(Completable.complete())
    private val stringSupplier = mock(StringSupplier::class.java)
    private val completableStatusReporter = spy(CompletableStatusReporter(stringSupplier))
    private val refreshInteractor = mock(RefreshInteractor::class.java)

    private val unit =
        AutoRefreshService(
            refreshInteractor,
            INTERVAL_MILLIS,
            completableStatusReporter
        )

    @Before
    fun beforeEach() {
        given(refreshInteractor.execution).willReturn(executionCompletable)
    }

    @Test
    fun `Refreshes local data when started`() {
        unit.start()

        then(executionCompletable).should(timeout(THREAD_SWITCH_TIMEOUT_MS))
            .subscribe(any<CompletableObserver>())
    }

    @Test
    fun `Refreshes local data periodically`() {
        unit.start()

        sleep(INTERVAL_MILLIS)
        then(executionCompletable).should(timeout(THREAD_SWITCH_TIMEOUT_MS).times(2))
            .subscribe(any<CompletableObserver>())

        sleep(INTERVAL_MILLIS)
        then(executionCompletable).should(timeout(THREAD_SWITCH_TIMEOUT_MS).times(3))
            .subscribe(any<CompletableObserver>())
    }

    // TODO stop()
    // TODO reports status
}