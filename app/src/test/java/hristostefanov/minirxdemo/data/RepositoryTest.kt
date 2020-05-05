package hristostefanov.minirxdemo.data

import hristostefanov.minirxdemo.business.DataSource
import hristostefanov.minirxdemo.business.Post
import io.reactivex.Single
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mockito.mock

class RepositoryTest {
    private val remoteDataSource = mock(DataSource::class.java)
    private val persistentDataSource = mock(DataSource::class.java)

    // dummies?
    private val post1 = Post("title1", 1)
    private val post2 = Post("title2", 2)

    private val remoteSource = Single.just(listOf(post1))
    private val persistentSource = Single.just(listOf(post2))

    @Test
    fun getAllPosts() {
        given(remoteDataSource.getAllPosts()).willReturn(remoteSource)
        given(persistentDataSource.getAllPosts()).willReturn(persistentSource)
        val repositoryUnderTest = Repository(remoteDataSource, persistentDataSource);

        val observer = repositoryUnderTest.getAllPosts().test()

        then(remoteDataSource).should().getAllPosts()
        then(persistentDataSource).should().getAllPosts()
    }
}