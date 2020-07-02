package hristostefanov.minirxdemo

import hristostefanov.minirxdemo.business.interactors.ObserveTenFirstPosts
import hristostefanov.minirxdemo.business.Post
import hristostefanov.minirxdemo.business.interactors.RequestRefreshLocalData
import hristostefanov.minirxdemo.business.User
import hristostefanov.minirxdemo.presentation.MainViewModel
import hristostefanov.minirxdemo.business.interactors.PostFace
import hristostefanov.minirxdemo.utilities.StringSupplier
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.schedulers.Schedulers
import junit.framework.Assert.fail
import org.junit.Test
import org.mockito.BDDMockito.*
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.util.concurrent.TimeUnit

private const val TIMEOUT_MS = 200L

class MainViewModelTest {
    private val listPostsInteractor = mock(ObserveTenFirstPosts::class.java)
    private val refreshInteractor = mock(RequestRefreshLocalData::class.java)
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
        given(listPostsInteractor.source()).willReturn(Observable.just(emptyList()))
        given(refreshInteractor.execution()).willReturn(Completable.complete())

        viewModelUnderTest.init()

        then(listPostsInteractor).should().source()
        then(listPostsInteractor).shouldHaveNoMoreInteractions()
        then(refreshInteractor).should().execution()
        then(refreshInteractor).shouldHaveNoMoreInteractions()
    }

    // TODO is this a rule?
    @Test
    fun `Subscribe to inputs on IO scheduler`() {
        val observableSpy = Mockito.spy(Observable.never<List<Post>>())
        given(listPostsInteractor.source()).willReturn(observableSpy)
        given(refreshInteractor.execution()).willReturn(Completable.complete())

        viewModelUnderTest.init()

        then(observableSpy).should().subscribeOn(Schedulers.io())
    }

    @Test
    fun `Refresh command`() {
        given(listPostsInteractor.source()).willReturn(Observable.never())
        given(refreshInteractor.execution()).willReturn(Completable.complete())

        viewModelUnderTest.init()
        viewModelUnderTest.refreshCommandObserver.onNext(Unit)

        // TODO check subscribing
        then(refreshInteractor).should(times(2)).execution()
        then(refreshInteractor).shouldHaveNoMoreInteractions()
    }

    /**
     * Rule: observable output properties are infinite
     */
    @Test
    fun `WHEN postList emits THEN will not complete`() {
        val listPostsObservable = Observable.concat(Observable.just(listOf(post1)), Observable.never())
        given(listPostsInteractor.source()).willReturn(listPostsObservable)
        given(refreshInteractor.execution()).willReturn(Completable.complete())

        val observer = viewModelUnderTest.postList.test()
        viewModelUnderTest.init()

        // expecting default value + payload
        observer.awaitCount(2)
        observer.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)
        observer.assertNotTerminated()
    }

    /**
     * Rule: observable output properties are infinite
     */
    @Test
    fun `WHEN errorMessage emits THEN will not complete`() {

        val refreshCompletable = spy(Completable.error(Throwable("error")))
        given(listPostsInteractor.source()).willReturn(Observable.never())
        given(refreshInteractor.execution()).willReturn(refreshCompletable)

        val observer = viewModelUnderTest.errorMessage.test()
        viewModelUnderTest.init()

        // expecting default value + payload
        observer.awaitCount(2)
        observer.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)
        observer.assertNotTerminated()
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
        val listPostsObservable = spy(Observable.concat(Observable.just(listOf(post1)), Observable.never()))
        given(listPostsInteractor.source()).willReturn(listPostsObservable)
        given(refreshInteractor.execution()).willReturn(Completable.complete())

        val postListObserver1 = viewModelUnderTest.postList.test()

        viewModelUnderTest.init()

        // await the default + new emission, assert the value count cause awaitCount may timeout
        postListObserver1.awaitCount(2).assertValueCount(2)
        // re-subscribe
        postListObserver1.dispose()
        val postListObserver2 = viewModelUnderTest.postList.test()

        postListObserver2
            .awaitCount(1) // getting only the latest emission
            .assertValue(listOf(
                PostFace(
                    post1.title,
                    "@${post1.user.username}"
                )
            ))

        then(listPostsInteractor).should(times(1)).source()
        // .subscribe() is called through a decorator returned by .subscribeOn()
        then(listPostsObservable).should(times(1)).subscribe(any<Observer<List<Post>>>())
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
    fun `Re-subscribing errorMessage`() {
        given(listPostsInteractor.source()).willReturn(Observable.never())
        val refreshCompletable = spy(Completable.error(Throwable("error")))
        given(refreshInteractor.execution()).willReturn(refreshCompletable)

        val errorMessageObserver1 = viewModelUnderTest.errorMessage.test()

        viewModelUnderTest.init()

        // await the default + new emission, assert the value count cause awaitCount may timeout
        errorMessageObserver1.awaitCount(2).assertValueCount(2)

        // re-subscribe
        errorMessageObserver1.dispose()
        val errorMessageObserver2 = viewModelUnderTest.errorMessage.test()

        errorMessageObserver2
            .awaitCount(1) // getting only the latest emission
            .assertValue("error")

        // .subscribe() is called through a decorator returned by .subscribeOn()
        then(refreshInteractor).should(times(1)).execution()
        then(refreshCompletable).should(times(1)).subscribe(any<CompletableObserver>())
    }


    /**
     * Rule: map [Post] to [PostFace]
     */
    @Test
    fun `Listing posts succeeds`() {
        val listPostsObservable = Observable.concat(Observable.just(listOf(post1)), Observable.never())
        given(listPostsInteractor.source()).willReturn(listPostsObservable)
        given(refreshInteractor.execution()).willReturn(Completable.complete()) // subscribed to initially


        val postListObserver = viewModelUnderTest.postList.test()
        viewModelUnderTest.init()

        // expecting the default value + payload
        postListObserver.awaitCount(2).assertValueAt(1, listOf(
            PostFace(
                "Title",
                "@username"
            )
        ))
    }

    /**
     *  Rule: map [Throwable] to [String]
     */
    @Test
    fun `Refreshing fails`() {
        val refreshCompletable = Completable.error(Throwable("error"))
        given(refreshInteractor.execution()).willReturn(refreshCompletable)
        given(listPostsInteractor.source()).willReturn(Observable.never()) // it's an infinite observable

        val errorMessageObserver = viewModelUnderTest.errorMessage.test()
        viewModelUnderTest.init()

        // expecting the default value + payload
        errorMessageObserver.awaitCount(2).assertValueAt(1, "error")
    }

    @Test
    fun `Refreshing succeeds`() {
        // TODO implement
        fail()
    }
}