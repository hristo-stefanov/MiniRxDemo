package hristostefanov.minirxdemo.business

import hristostefanov.minirxdemo.presentation.PostFace
import io.reactivex.Observable

interface PostGateway {
    fun observeQueryAllPosts(): Observable<List<PostFace>>
    fun deleteAll()
    fun insertAll(list: List<Post>)
}