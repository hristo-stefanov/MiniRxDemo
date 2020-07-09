package hristostefanov.minirxdemo.business.interactors

import hristostefanov.minirxdemo.business.gateways.local.PostDAO
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ObservePosts @Inject constructor(private val postDAO: PostDAO) {
    val source: Observable<List<PostSummary>> by lazy {
        postDAO.observePostAndUserSortedByTitleInTx()
            .subscribeOn(Schedulers.io())
            .map { list ->
            list
                .map {
                    PostSummary(
                        it.post.title,
                        it.user.username
                    )
                }
        }
    }
}

data class PostSummary(
    val title: String,
    val username: String
)