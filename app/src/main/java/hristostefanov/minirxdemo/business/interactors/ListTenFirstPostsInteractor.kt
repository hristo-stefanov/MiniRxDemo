package hristostefanov.minirxdemo.business.interactors

import hristostefanov.minirxdemo.business.gateways.local.PostDAO
import io.reactivex.Observable
import javax.inject.Inject

private const val MAX_POST_COUNT = 10

class ListTenFirstPostsInteractor @Inject constructor(private val postGateway: PostDAO) {
    fun query(): Observable<List<PostFace>> {
        return postGateway.getPostAndUser().map { list ->
            list
                .take(MAX_POST_COUNT)
                .map {
                    PostFace(
                        it.post.title,
                        "@${it.user.username}"
                    )
                }
        }
    }
}