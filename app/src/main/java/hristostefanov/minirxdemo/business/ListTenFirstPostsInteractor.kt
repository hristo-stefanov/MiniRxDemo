package hristostefanov.minirxdemo.business

import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

private const val MAX_POST_COUNT = 10

class ListTenFirstPostsInteractor @Inject constructor(private val repository: Repository) {
    fun query(): Observable<List<PostInfo>> {
        return repository.getAllPosts()
            .concatMapSingle { posts: List<Post> ->
                Observable.fromIterable(posts.take(MAX_POST_COUNT)).concatMapSingle { post: Post ->
                    repository.getUserById(post.userId).map { user: User ->
                        PostInfo(user.username, post.title)
                    }
                }.toList()
            }
    }

    fun refresh(): Completable {
        return repository.refresh()
    }
}