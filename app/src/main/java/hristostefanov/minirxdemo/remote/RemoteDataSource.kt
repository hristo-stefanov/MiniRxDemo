package hristostefanov.minirxdemo.remote

import hristostefanov.minirxdemo.business.DataSource
import hristostefanov.minirxdemo.business.Post
import hristostefanov.minirxdemo.business.User
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val service: Service) {
    fun getAllPosts(): Single<List<Post>> {
        return service.getAllPosts()
            .map {
                it.map { postDTO ->
                    Post(postDTO.id, postDTO.title, postDTO.body, postDTO.userId)
                }
            }
    }

    fun getUserById(userId: Int): Single<User> {
        return service.getUserById(userId)
            .map {
                User(it.id, it.username)
            }
    }
}