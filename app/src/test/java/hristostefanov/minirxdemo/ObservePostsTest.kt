package hristostefanov.minirxdemo

import hristostefanov.minirxdemo.business.interactors.ObservePosts
import io.reactivex.Observable
import junit.framework.Assert
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock


class ObservePostsTest {

//    private val repository = mock(Repository::class.java)
//


    // TODO is this a rule?
    @Test
    fun `Subscribe to inputs on IO scheduler`() {
        Assert.fail()
/*        val observableSpy = Mockito.spy(Observable.never<List<Post>>())
        given(listPostsInteractor.source()).willReturn(observableSpy)
        given(refreshInteractor.execution()).willReturn(Completable.complete())

        viewModelUnderTest.init()

        then(observableSpy).should().subscribeOn(Schedulers.io())*/
    }


//    @Test
//    // Rule: List 10 first posts.
//    fun `When receiving a list of 11 Posts Then will emit a mapped list of 10 PostInfo`() {
//        val posts = (1..11).map {
//            Post(it, it.toString(), "body $it", User(42, "user42"))
//        }
//
//        given(repository.getAllPosts()).willReturn(Observable.just(posts))
//        val interactorUnderTest =
//            ObservePosts(
//                repository
//            )
//
//        val observer = interactorUnderTest.source().test()
//
//        val result = observer.values()[0]
//        assertThat(result.size, equalTo(10))
//        assertThat(result[0].title, equalTo("1"))
//        assertThat(result[0].user.username, equalTo("user42"))
//    }
}