package hristostefanov.minirxdemo

import hristostefanov.minirxdemo.business.ListTenFirstPostsInteractor
import hristostefanov.minirxdemo.business.Post
import hristostefanov.minirxdemo.business.Repository
import hristostefanov.minirxdemo.business.User
import hristostefanov.minirxdemo.remote.RemoteDataSource
import io.reactivex.Single
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock


class ListTenFirstPostsInteractorTest {

    private val remoteDataSource = mock(Repository::class.java)

    @Test
    // Rule: List 10 first posts.
    fun `When receiving a list of 11 Posts Then will emit a mapped list of 10 PostInfo`() {
        val posts = (1..11).map {
            Post(it, it.toString(), "body $it", 42)
        }
        given(remoteDataSource.getAllPosts()).willReturn(Single.just(posts))
        given(remoteDataSource.getUserById(42)).willReturn(Single.just(User(42, "username")))

        val interactorUnderTest = ListTenFirstPostsInteractor(remoteDataSource)

        val observer = interactorUnderTest.query().test()

        val result = observer.values()[0]
        assertThat(result.size, equalTo(10))
        assertThat(result[0].title, equalTo("1"))
        assertThat(result[0].username, equalTo("username"))
    }
}