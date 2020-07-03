package hristostefanov.minirxdemo.business.interactors

import hristostefanov.minirxdemo.business.gateways.local.PostDAO
import io.reactivex.Observable
import javax.inject.Inject

class ObservePostsSortedByTitle @Inject constructor(private val postDAO: PostDAO) {
    val source: Observable<List<PostFace>> by lazy {
        postDAO.observePostAndUserSortedByTitleInTx().map { list ->
            list
                .map {
                    PostFace(
                        it.post.title,
                        "@${it.user.username}"
                    )
                }
        }
    }
}

data class PostFace(
    val title: String,
    val username: String
)