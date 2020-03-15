package hristostefanov.minirxdemo.remote

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface Service {
    @GET("/posts")
    fun getAllPosts(): Single<List<PostDTO>>

    @GET("/users/{id}")
    fun getUserById(@Path("id") userId: Int): Single<UserDTO>
}