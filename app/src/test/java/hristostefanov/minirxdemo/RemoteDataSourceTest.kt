package hristostefanov.minirxdemo

import hristostefanov.minirxdemo.remote.PostDTO
import hristostefanov.minirxdemo.remote.RemoteDataSource
import hristostefanov.minirxdemo.remote.Service
import hristostefanov.minirxdemo.remote.UserDTO
import io.reactivex.Single
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mockito.mock

class RemoteDataSourceTest {
    private val service = mock(Service::class.java)

    // NOTE: using await because of switching threads

    @Test
    fun `When subscribing on getAllPosts Then will query provided Service and map PostDTO to Post`() {
        val postDTO = PostDTO(2, 1, "Title", "Body")
        given(service.getAllPosts()).willReturn(Single.just(listOf(postDTO)))
        val remoteDataSourceUnderTest = RemoteDataSource(service)

        val observer = remoteDataSourceUnderTest.getAllPosts().test()

        observer.awaitCount(1)
        then(service).should().getAllPosts()
        then(service).shouldHaveNoMoreInteractions()
        assertThat((observer.values()[0][0].userId), equalTo(2))
        assertThat((observer.values()[0][0].title), equalTo("Title"))
    }

    @Test
    fun `When subscribing on getUserById Then will query provided Service and map UserDTO to User`() {
        val userDTO = UserDTO(42, "username")
        given(service.getUserById(42)).willReturn(Single.just(userDTO))
        val remoteDataSourceUnderTest = RemoteDataSource(service)

        val observer = remoteDataSourceUnderTest.getUserById(42).test()

        observer.awaitCount(1)
        then(service).should().getUserById(42)
        then(service).shouldHaveNoMoreInteractions()
        assertThat((observer.values()[0].username), equalTo("username"))
    }

    @Test
    // Rule: Should Subscribe upstream on Scheduler.io()
    fun `When subscribed on getAllPosts Then will run upstream on a worker thread`() {
        var thread: Thread? = null
        val singleSource = Single.just(emptyList<PostDTO>()).doOnSubscribe {
            thread = Thread.currentThread()
        }
        given(service.getAllPosts()).willReturn(singleSource)
        val remoteDataSourceUnderTest =
            RemoteDataSource(service)

        val observer = remoteDataSourceUnderTest.getAllPosts().test()

        observer.awaitCount(1)
        assertThat(thread?.name, equalTo("RxCachedThreadScheduler-1"))
    }

    @Test
    // Rule: Should Subscribe upstream on Scheduler.io()
    fun `When subscribed on getUserById Then will run upstream on a worker thread`() {
        var thread: Thread? = null
        val singleSource = Single.just(
            UserDTO(
                42,
                "username"
            )
        )
            .doOnSubscribe {
                thread = Thread.currentThread()
            }
        given(service.getUserById(42)).willReturn(singleSource)
        val remoteDataSourceUnderTest = RemoteDataSource(service)

        val observer = remoteDataSourceUnderTest.getUserById(42).test()

        observer.awaitCount(1)
        assertThat(thread?.name, equalTo("RxCachedThreadScheduler-1"))
    }
}