package hristostefanov.minirxdemo.business.interactors

import hristostefanov.minirxdemo.any
import hristostefanov.minirxdemo.business.entities.Post
import hristostefanov.minirxdemo.business.entities.User
import hristostefanov.minirxdemo.business.gateways.local.PostDAO
import hristostefanov.minirxdemo.business.gateways.local.UserDAO
import hristostefanov.minirxdemo.business.gateways.remote.PostResource
import hristostefanov.minirxdemo.business.gateways.remote.Service
import hristostefanov.minirxdemo.business.gateways.remote.UserResource
import io.reactivex.Single
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import java.util.concurrent.Executor

class RefreshInteractorTest {

    private val userDAO = mock(UserDAO::class.java)
    private val postDAO = mock(PostDAO::class.java)
    private val service = mock(Service::class.java)
    private val txExecutor = spy(Executor {
        it.run()
    })

    private val unit: RefreshInteractor by lazy {
        RefreshInteractor(service, userDAO, postDAO, txExecutor)
    }

    @Test
    fun `Refreshes local data from remote service`() {
        val user1 = UserResource(11, "username")
        val post1 = PostResource(1, 11, "title", "body")
        given(service.getAllUsers()).willReturn(Single.just(listOf(user1)))
        given(service.getAllPosts()).willReturn(Single.just(listOf(post1)))

        val observer = unit.execution.test()

        observer.await().assertComplete()
        then(txExecutor).should().execute(any())
        then(userDAO).should().deleteAll()
        then(postDAO).should().deleteAll()
        then(service).should().getAllPosts()
        then(service).should().getAllUsers()
        then(postDAO).should().insert(listOf(Post(1, "title", "body", 11)))
        then(userDAO).should().insert(listOf(User(11, "username")))
    }
}