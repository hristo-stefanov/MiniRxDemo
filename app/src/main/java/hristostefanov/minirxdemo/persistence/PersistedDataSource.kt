package hristostefanov.minirxdemo.persistence

import hristostefanov.minirxdemo.business.Post
import hristostefanov.minirxdemo.business.User
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class PersistedDataSource @Inject constructor(private val database: Database) {
    fun getAllPosts(): Observable<List<Post>> {
        return database.postDao().getAll()
            .map { list ->
                list.map {
                    Post(title = it.title, userId = it.userId, id = it.id, body = it.body)
                }
            }
    }

    fun getUserById(userId: Int): Single<User> {
        return database.userDao().getUserById(userId).map {
            User(it.id, it.username)
        }
    }

    private fun savePosts(posts: List<Post>) {
        database.postDao().insert(posts.map {
            PostEntity(it.id, it.title, it.body, it.userId)
        })
    }

    private fun saveUsers(users: List<User>) {
        database.userDao().insert(users.map {
            UserEntity(it.id, it.username)
        })
    }

    fun saveUser(user: User) {
        database.userDao().insert(UserEntity(user.id, user.username))
    }

    fun refreshTx(posts: List<Post>, users: List<User>) {
        // in Room transactions are synchronous only to guarantee using a single thread
        database.runInTransaction {
            database.userDao().deleteAll()
            database.postDao().deleteAll()
            savePosts(posts)
            saveUsers(users)
        }
    }
}