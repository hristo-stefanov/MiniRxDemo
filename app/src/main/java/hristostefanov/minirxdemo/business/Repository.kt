package hristostefanov.minirxdemo.business

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface Repository {
    fun getAllPosts(): Observable<List<Post>>
    fun getUserById(userId: Int): Single<User>
    fun refresh(): Completable
}