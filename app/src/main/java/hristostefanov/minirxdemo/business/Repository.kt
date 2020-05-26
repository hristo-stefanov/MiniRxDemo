package hristostefanov.minirxdemo.business

import io.reactivex.Single

interface Repository {
    fun getAllPosts(): Single<List<Post>>
    fun getUserById(userId: Int): Single<User>
}