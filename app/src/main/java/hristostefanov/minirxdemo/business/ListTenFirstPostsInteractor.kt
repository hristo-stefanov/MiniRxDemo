package hristostefanov.minirxdemo.business

import io.reactivex.Observable
import javax.inject.Inject

private const val MAX_POST_COUNT = 10

class ListTenFirstPostsInteractor @Inject constructor(private val repository: Repository) {
    fun query(): Observable<List<Post>> {
        return repository.getAllPosts().map { list ->
            list.take(MAX_POST_COUNT)
        }
    }
}