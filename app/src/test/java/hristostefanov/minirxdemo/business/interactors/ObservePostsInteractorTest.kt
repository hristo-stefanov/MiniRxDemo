package hristostefanov.minirxdemo.business.interactors

import hristostefanov.minirxdemo.business.entities.Post
import hristostefanov.minirxdemo.business.entities.User
import hristostefanov.minirxdemo.business.gateways.local.PostAndUser
import hristostefanov.minirxdemo.business.gateways.local.PostDAO
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy


class ObservePostsInteractorTest {
    private val dao = mock(PostDAO::class.java)
    private val unit = ObservePostsInteractor(dao)

    // TODO is the name ok?
    @Test
    fun `Maps @PostDAO#observePostAndUserSortedByTitleInTx emissions to @PostSummary`() {
        val observable = Observable.concat(Observable.just(listOf(
            PostAndUser(Post(1, "title", "body",11),
                User(11, "username"))
        )), Observable.never())
        given(dao.observePostAndUserSortedByTitleInTx()).willReturn(observable)

        val observer = unit.source.test()

        then(dao).should().observePostAndUserSortedByTitleInTx()
        observer.awaitCount(1).assertValueCount(1)
            .assertValueAt(0, listOf(PostSummary("title", "username")))
    }

    @Test
    fun `Subscribes on IO scheduler`() {
        val observableSpy = spy(Observable.never<List<PostAndUser>>())
        given(dao.observePostAndUserSortedByTitleInTx()).willReturn(observableSpy)

        unit.source

        then(observableSpy).should().subscribeOn(Schedulers.io())
    }
}