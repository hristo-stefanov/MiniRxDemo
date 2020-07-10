package hristostefanov.minirxdemo.business.interactors

import hristostefanov.minirxdemo.THREAD_SWITCH_TIMEOUT_MS
import hristostefanov.minirxdemo.any
import hristostefanov.minirxdemo.utilities.StringSupplier
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.*
import java.lang.Thread.sleep

private const val INTERVAL_MILLIS = THREAD_SWITCH_TIMEOUT_MS * 3

class AutoRefreshServiceTest {

    private val completable = spy(Completable.complete())
    private val stringSupplier = object : StringSupplier {
        override fun get(resId: Int): String = ""
    }

    private val observeBackgroundOperationStatus =
        spy(ObserveBackgroundOperationStatus(stringSupplier))
    private val refreshInteractor = mock(RefreshInteractor::class.java)

    private val unit =
        AutoRefreshService(
            observeBackgroundOperationStatus, refreshInteractor,
            INTERVAL_MILLIS
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
}