package hristostefanov.minirxdemo.business

import io.reactivex.Single

interface DataSource {
    fun getAllPosts(): Single<List<Post>>
    fun getUserById(userId: Int): Single<User>
}