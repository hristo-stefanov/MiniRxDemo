package hristostefanov.minirxdemo.persistence

import hristostefanov.minirxdemo.business.Post
import hristostefanov.minirxdemo.business.PostGateway
import hristostefanov.minirxdemo.presentation.PostFace
import io.reactivex.Observable
import javax.inject.Inject

class PostGatewayImpl @Inject constructor(private val database: Database): PostGateway {
    override fun observeQueryAllPosts(): Observable<List<PostFace>> {
        return database.postDao().getPostAndUser()
            .map { list ->
                list.map {
                    PostFace(it.post.title, "@${it.user.username}")
                }
            }
    }

    override fun deleteAll() {
        database.postDao().deleteAll()
    }

    override fun insertAll(list: List<Post>) {
        database.postDao().insert(list.map {
            PostEntity(it.id, it.title, it.body, it.userId)
        })
    }
}