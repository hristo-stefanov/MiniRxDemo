package hristostefanov.minirxdemo.business.gateways.remote

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface Service {
    @GET("/posts")
    fun getAllPosts(): Single<List<PostResource>>

    @GET("/users")
    fun getAllUsers(): Single<List<UserResource>>

    @GET("/users/{id}")
    fun getUserById(@Path("id") userId: Int): Single<UserResource>
}