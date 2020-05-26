package hristostefanov.minirxdemo.data

import hristostefanov.minirxdemo.any
import hristostefanov.minirxdemo.business.Post
import hristostefanov.minirxdemo.persistence.PersistedDataSource
import hristostefanov.minirxdemo.remote.RemoteDataSource
import io.reactivex.Maybe
import io.reactivex.MaybeObserver
import io.reactivex.Single
import io.reactivex.SingleObserver
import org.junit.Test
import org.mockito.BDDMockito.*

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

        observer.assertValue(listOf(post1, post2))
        then(persistentObservable).should().subscribe(any<MaybeObserver<List<Post>>>())
        then(remoteObservable).should(times(0)).subscribe(any<SingleObserver<List<Post>>>())
    }

    @Test
    fun getAllPostsFromRemoteDataSource() {
        val persistentObservable = spy(Maybe.empty<List<Post>>())
        given(persistentDataSource.getAllPosts()).willReturn(persistentObservable)
        val remoteObservable = spy(Single.just(listOf(post1, post2)))
        given(remoteDataSource.getAllPosts()).willReturn(remoteObservable)
        val repositoryUnderTest = RepositoryImpl(remoteDataSource, persistentDataSource);

        val observer = repositoryUnderTest.getAllPosts().test()
        observer.assertValue(listOf(post1, post2))

        then(persistentObservable).should().subscribe(any<MaybeObserver<List<Post>>>())
        then(remoteObservable).should().subscribe(any<SingleObserver<List<Post>>>())
    }
}