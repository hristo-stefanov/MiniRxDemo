package hristostefanov.minirxdemo

import hristostefanov.minirxdemo.business.ListTenFirstPostsInteractor
import hristostefanov.minirxdemo.business.Post
import hristostefanov.minirxdemo.business.RefreshInteractor
import hristostefanov.minirxdemo.business.User
import hristostefanov.minirxdemo.presentation.MainViewModel
import hristostefanov.minirxdemo.presentation.PostFace
import hristostefanov.minirxdemo.util.StringSupplier
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import org.mockito.BDDMockito.*
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.lang.Thread.sleep

private const val TIMEOUT = 200L

class MainViewModelTest {
    private val listPostsInteractor = mock(ListTenFirstPostsInteractor::class.java)
    private val refreshInteractor = mock(RefreshInteractor::class.java)
    private val stringSupplier = object : StringSupplier {
        override fun get(resId: Int): String = "Unknown error"
    }
    val viewModelUnderTest: MainViewModel by lazy {
        MainViewModel(listPostsInteractor, refreshInteractor, stringSupplier)
    }

    // test data
    val post1 = Post(1, "Title", "body", User(1, "username"))

    @Test
    fun `Create`() {
        given(listPostsInteractor.query()).willReturn(Observable.just(emptyList()))
        given(refreshInteractor.execute()).willReturn(Completable.complete())

        viewModelUnderTest

        then(listPostsInteractor).should().query()
        then(listPostsInteractor).shouldHaveNoMoreInteractions()
        then(refreshInteractor).should().execute()
        then(refreshInteractor).shouldHaveNoMoreInteractions()
    }


    @Test
    fun `Refresh`() {
        given(listPostsInteractor.query()).willReturn(Observable.never())
        given(refreshInteractor.execute()).willReturn(Completable.complete())

        viewModelUnderTest.refreshObserver.onNext(Unit)

        then(refreshInteractor).should(times(2)).execute()
        then(refreshInteractor).shouldHaveNoMoreInteractions()
    }

    @Test
    // Rule: observable properties are infinite
    fun `First subscription`() {
        given(listPostsInteractor.query()).willReturn(Observable.never())
        given(refreshInteractor.execute()).willReturn(Completable.complete())

        val postListObserver = viewModelUnderTest.postList.test()
        val errorMessageObserver = viewModelUnderTest.errorMessage.test()

        sleep(TIMEOUT) // wait enough for the I/O to assert observers not terminated
        postListObserver.assertValueCount(1).assertNotTerminated() // emits only the default value
        errorMessageObserver.assertValueCount(1)
            .assertNotTerminated() // emits only the default value
    }

    @Test
    fun `Subscribe upstream on IO scheduler`() {
        val observableSpy = Mockito.spy(Observable.never<List<Post>>())
        given(listPostsInteractor.query()).willReturn(observableSpy)
        given(refreshInteractor.execute()).willReturn(Completable.complete())

        viewModelUnderTest

        then(observableSpy).should().subscribeOn(Schedulers.io())
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
    fun `Re-subscribing postList`() {
        given(refreshInteractor.execute()).willReturn(Completable.complete())
        val postsSubject = spy(PublishSubject.create<List<Post>>())
        given(listPostsInteractor.query()).willReturn(postsSubject)
        val postListObserver1 = viewModelUnderTest.postList.test()
        postListObserver1.awaitCount(1) // await the default value
        postsSubject.onNext(listOf(post1)) // emit some post
        postListObserver1.awaitCount(2) // await the new emission

        // re-subscribe
        postListObserver1.dispose()
        val postListObserver2 = viewModelUnderTest.postList.test()

        postListObserver2
            .awaitCount(1) // getting only the new emission
            .assertValue(listOf(PostFace(post1.title, "@${post1.user.username}")))

        then(listPostsInteractor).should(times(1)).query()
        // .subscribe() is called through a decorator returned by .subscribeOn()
        then(postsSubject).should(times(1)).subscribe(any<Observer<List<Post>>>())
    }

    /**
     * Rule: should not re-query when resubscribing
     * Rule: should keep the state when resubscribing
     */
    @Test
    fun `Re-subscribing errorMessage`() {
        given(listPostsInteractor.query()).willReturn(Observable.never())
        val refreshSubject = spy(PublishSubject.create<Unit>())
        given(refreshInteractor.execute()).willReturn(Completable.fromObservable(refreshSubject))
        val errorMessageObserver1 = viewModelUnderTest.errorMessage.test()
        errorMessageObserver1.awaitCount(1) // await the default value
        refreshSubject.onError(Throwable("error")) // emit some error
        errorMessageObserver1.awaitCount(2) // await the error message

        // re-subscribe
        errorMessageObserver1.dispose()
        val errorMessageObserver2 = viewModelUnderTest.errorMessage.test()

        errorMessageObserver2
            .awaitCount(1) // getting only the new emission
            .assertValue("error")
        // .subscribe() is called through a decorator returned by .subscribeOn()
        then(refreshInteractor).should(times(1)).execute()
        then(refreshSubject).should(times(1)).subscribe(any<Observer<Unit>>())
    }


    /**
     * Rule: map [Post] to [PostFace]
     */
    @Test
    fun `Listing posts succeeds`() {
        given(refreshInteractor.execute()).willReturn(Completable.complete()) // subscribed to initially
        val listPostsSubject = PublishSubject.create<List<Post>>()
        given(listPostsInteractor.query()).willReturn(listPostsSubject)
        val postListObserver = viewModelUnderTest.postList.test()
        postListObserver.awaitCount(1) // await the default value

        listPostsSubject.onNext(listOf(post1))

        postListObserver.awaitCount(2).assertValueAt(1, listOf(PostFace("Title", "@username")))
    }

    @Test
            /**
             *  Rule: map [Throwable] to [String]
             */
    fun `Refreshing fails`() {
        given(listPostsInteractor.query()).willReturn(Observable.never()) // it's an infinite observable
        val refreshSubject = PublishSubject.create<Unit>()
        given(refreshInteractor.execute()).willReturn(Completable.fromObservable(refreshSubject))
        val errorMessageObserver = viewModelUnderTest.errorMessage.test()
        errorMessageObserver.awaitCount(1) // await the default value

        refreshSubject.onError(Throwable("error details"))

        errorMessageObserver.awaitCount(2).assertValueAt(1, "error details")
    }
}