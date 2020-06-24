package hristostefanov.minirxdemo

import hristostefanov.minirxdemo.business.ListTenFirstPostsInteractor
import hristostefanov.minirxdemo.business.Post
import hristostefanov.minirxdemo.business.Repository
import hristostefanov.minirxdemo.business.User
import io.reactivex.Observable
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock


class ListTenFirstPostsInteractorTest {

    private val repository = mock(Repository::class.java)

    @Test
    // Rule: List 10 first posts.
    fun `When receiving a list of 11 Posts Then will emit a mapped list of 10 PostInfo`() {
        val posts = (1..11).map {
            Post(it, it.toString(), "body $it", User(42, "user42"))
        }

        given(repository.getAllPosts()).willReturn(Observable.just(posts))
        val interactorUnderTest = ListTenFirstPostsInteractor(repository)

        val observer = interactorUnderTest.query().test()

        val result = observer.values()[0]
        assertThat(result.size, equalTo(10))
        assertThat(result[0].title, equalTo("1"))
        assertThat(result[0].user.username, equalTo("user42"))
    }
}