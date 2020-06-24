package hristostefanov.minirxdemo.business

import hristostefanov.minirxdemo.presentation.PostFace
import io.reactivex.Observable
import javax.inject.Inject

private const val MAX_POST_COUNT = 10

class ListTenFirstPostsInteractor @Inject constructor(private val postGateway: PostGateway) {
    fun query(): Observable<List<PostFace>> {
        return postGateway.observeQueryAllPosts().map { list ->
            list.take(MAX_POST_COUNT)
        }
    }
}