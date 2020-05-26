package hristostefanov.minirxdemo.data

import hristostefanov.minirxdemo.any
import hristostefanov.minirxdemo.business.DataSource
import hristostefanov.minirxdemo.business.Post
import hristostefanov.minirxdemo.persistence.PersistedDataSource
import hristostefanov.minirxdemo.remote.RemoteDataSource
import io.reactivex.*
import org.junit.Test
import org.mockito.BDDMockito.*
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy

class RepositoryImplTest {
    private val remoteDataSource = mock(RemoteDataSource::class.java)
    private val persistentDataSource = mock(PersistedDataSource::class.java)

    // test data
    private val post1 = Post(1, "title1", "body2", 1)
    private val post2 = Post(2, "title2", "body2", 2)

    @Test
    fun getAllPostsFromPersistentDataSource() {
        val persistentObservable = spy(Maybe.just(listOf(post1, post2)))
        given(persistentDataSource.getAllPosts()).willReturn(persistentObservable)
        val remoteObservable = spy(Single.just<List<Post>>(emptyList()))
        given(remoteDataSource.getAllPosts()).willReturn(remoteObservable)
        val repositoryUnderTest = RepositoryImpl(remoteDataSource, persistentDataSource);

        val observer = repositoryUnderTest.getAllPosts().test()

        observer.assertValue(listOf(post1,post2))
        then(persistentObservable).should().toObservable()
        then(persistentObservable).should().subscribe(any<MaybeObserver<List<Post>>>())
        // actually #subscribeActual() is also invoked (by #subscribe())
        // then(persistentObservable).shouldHaveNoMoreInteractions()
        then(remoteObservable).should().toObservable()
        then(remoteObservable).shouldHaveNoMoreInteractions()
    }

    @Test
    fun getAllPostsFromRemoteDataSource() {
        val persistentObservable = spy(Maybe.empty<List<Post>>())
        given(persistentDataSource.getAllPosts()).willReturn(persistentObservable)
        val remoteObservable = spy(Single.just(listOf(post1, post2)))
        given(remoteDataSource.getAllPosts()).willReturn(remoteObservable)
        val repositoryUnderTest = RepositoryImpl(remoteDataSource, persistentDataSource);

        val observer = repositoryUnderTest.getAllPosts().test()
        observer.assertValue(listOf(post1,post2))

        then(persistentObservable).should().toObservable()
        then(persistentObservable).should().subscribe(any<MaybeObserver<List<Post>>>())
        // actually #subscribeActual() is also invoked (by #subscribe())  but it's protected
        // then(persistentObservable).shouldHaveNoMoreInteractions()
        then(remoteObservable).should().toObservable()
        then(remoteObservable).should().subscribe(any<SingleObserver<List<Post>>>())
        // actually #subscribeActual() is also invoked (by #subscribe()) but it's protected
        // then(remoteObservable).shouldHaveNoMoreInteractions()
    }
}