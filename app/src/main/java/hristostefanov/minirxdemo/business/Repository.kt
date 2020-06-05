package hristostefanov.minirxdemo.business

import io.reactivex.Completable
import io.reactivex.Observable

interface Repository {
    fun getAllPosts(): Observable<List<Post>>
    fun refresh(): Completable
}