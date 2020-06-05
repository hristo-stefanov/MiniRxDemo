package hristostefanov.minirxdemo.persistence

import hristostefanov.minirxdemo.business.Post
import hristostefanov.minirxdemo.business.User
import hristostefanov.minirxdemo.remote.PostDTO
import hristostefanov.minirxdemo.remote.UserDTO
import io.reactivex.Observable
import javax.inject.Inject

class PersistedDataSource @Inject constructor(private val database: Database) {
    fun getAllPosts(): Observable<List<Post>> {
        return database.postDao().getPostAndUser()
            .map { list ->
                list.map {
                    val user = User(it.user.id, it.user.username)
                    Post(id = it.post.id, title = it.post.title, user = user,  body = it.post.body)
                }
            }
    }

    fun refreshInTx(posts: List<PostDTO>, users: List<UserDTO>) {
        // in Room, transactions are synchronous only (to guarantee using a single thread)
        database.runInTransaction {
            database.userDao().deleteAll()
            database.postDao().deleteAll()
            database.postDao().insert(posts.map {
                PostEntity(it.id, it.title, it.body, it.userId)
            })
            database.userDao().insert(users.map {
                UserEntity(it.id, it.username)
            })
        }
    }
}