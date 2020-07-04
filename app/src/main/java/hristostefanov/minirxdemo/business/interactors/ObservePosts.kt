package hristostefanov.minirxdemo.business.interactors

import hristostefanov.minirxdemo.business.gateways.local.PostDAO
import io.reactivex.Observable
import javax.inject.Inject

class ObservePosts @Inject constructor(private val postDAO: PostDAO) {
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

// TODO DisplayablePost?
data class PostFace(
    val title: String,
    val username: String
)