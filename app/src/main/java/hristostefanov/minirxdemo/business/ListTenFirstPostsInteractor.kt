package hristostefanov.minirxdemo.business

import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named

private const val MAX_POST_COUNT = 10

class ListTenFirstPostsInteractor @Inject constructor(private val dataSource: Repository) {
    fun query(): Single<List<PostInfo>> {
        return dataSource.getAllPosts()
            // expand the list into individual emissions
            .flatMapObservable {
                Observable.fromIterable(it.take(MAX_POST_COUNT))
            }
            // Using concatMap because the order of posts is important
            .concatMapSingle { post ->
                dataSource.getUserById(post.userId)
                    .map { user -> PostInfo(user.username, post.title) }
            }
            .toList()
    }
}