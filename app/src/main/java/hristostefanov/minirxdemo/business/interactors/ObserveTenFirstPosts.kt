package hristostefanov.minirxdemo.business.interactors

import hristostefanov.minirxdemo.business.gateways.local.PostDAO
import io.reactivex.Observable
import javax.inject.Inject

private const val MAX_POST_COUNT = 10

class ObserveTenFirstPosts @Inject constructor(private val postDAO: PostDAO) {
    val source: Observable<List<PostFace>> by lazy {
        postDAO.getPostAndUserInTx().map { list ->
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

data class PostFace(
    val title: String,
    val username: String
)