package hristostefanov.minirxdemo

import hristostefanov.minirxdemo.business.ListTenFirstPostsInteractor
import hristostefanov.minirxdemo.business.PostInfo
import hristostefanov.minirxdemo.presentation.MainViewModel
import hristostefanov.minirxdemo.util.StringSupplier
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.mockito.BDDMockito.*
import org.mockito.Mockito
import org.mockito.Mockito.mock


class MainViewModelTest {
    private val interactor = mock(ListTenFirstPostsInteractor::class.java)
    private val stringSupplier = object : StringSupplier {
        override fun get(resId: Int): String = "Unknown error"
    }

    @Test
    fun `Create`() {
        given(interactor.query()).willReturn(Single.just(emptyList()))

        val viewModelUnderTest = MainViewModel(interactor, stringSupplier)

        then(interactor).should().query()
        then(interactor).shouldHaveNoMoreInteractions()
    }


    @Test
    fun `Refresh`() {
        given(interactor.query()).willReturn(Single.just(emptyList()))
        val viewModelUnderTest = MainViewModel(interactor, stringSupplier)

        viewModelUnderTest.refreshObserver.onNext(Unit)

        then(interactor).should(Mockito.times(2)).query()
        then(interactor).shouldHaveNoMoreInteractions()
    }

    @Test
    // Rule: observable properties are infinite
    fun `First subscription`() {
        given(interactor.query()).willReturn(Single.error(Throwable()), Single.just(emptyList()))
        val viewModelUnderTest = MainViewModel(interactor, stringSupplier)

        val postListObserver = viewModelUnderTest.postList.test()
        val errorMessageObserver = viewModelUnderTest.errorMessage.test()


        postListObserver.assertValueCount(1).assertNotTerminated()
        errorMessageObserver.assertValueCount(1).assertNotTerminated()
    }

    @Test
    // Rule: should not re-query when resubscribing
    // Rule: should keep the state when resubscribing
    fun `Re-subscription`() {
        given(interactor.query()).willReturn(
            Single.error(Throwable("error")),
            Single.just(emptyList())
        )
        val viewModelUnderTest = MainViewModel(interactor, stringSupplier)
        val disposable = CompositeDisposable().apply {
            add(viewModelUnderTest.postList.subscribe())
            add(viewModelUnderTest.errorMessage.subscribe())
        }

        disposable.dispose()

        viewModelUnderTest.postList.test().assertValue(emptyList())
        viewModelUnderTest.errorMessage.test().assertValue("error")
        then(interactor).should(times(1)).query()
    }


    @Test
    // Rule: map PostInfo to PostFace
    fun `Querying succeeds`() {
        val postInfo = PostInfo("username", "Title")
        given(interactor.query()).willReturn(Single.just(listOf(postInfo)))
        val viewModelUnderTest = MainViewModel(interactor, stringSupplier)

        val postListObserver = viewModelUnderTest.postList.test()

        assertThat(postListObserver.values()[0][0].title, equalTo("Title"))
        assertThat(postListObserver.values()[0][0].username, equalTo("@username"))
    }

    @Test
    // Rule: map Throwable to String
    fun `Querying fails`() {
        given(interactor.query()).willReturn(Single.error(Throwable("error details")))
        val viewModelUnderTest = MainViewModel(interactor, stringSupplier)

        // subscribe to both to trigger the query
        val errorMessageObserver = viewModelUnderTest.errorMessage.test()

        errorMessageObserver.assertValue("error details")
    }
}