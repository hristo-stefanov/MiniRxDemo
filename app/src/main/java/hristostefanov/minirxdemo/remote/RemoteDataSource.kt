package hristostefanov.minirxdemo.remote

import hristostefanov.minirxdemo.business.DataSource
import hristostefanov.minirxdemo.business.Post
import hristostefanov.minirxdemo.business.User
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val service: Service) : DataSource {
    override fun getAllPosts(): Single<List<Post>> {
        return service.getAllPosts()
            .subscribeOn(Schedulers.io())
            .map {
                it.map { postDTO ->
                    Post(postDTO.title, postDTO.userId)
                }
            }
    }

    override fun getUserById(userId: Int): Single<User> {
        return service.getUserById(userId)
            .subscribeOn(Schedulers.io())
            .map {
                User(it.username)
            }
    }
}