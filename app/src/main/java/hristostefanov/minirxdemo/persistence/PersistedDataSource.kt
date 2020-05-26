package hristostefanov.minirxdemo.persistence

import hristostefanov.minirxdemo.business.Post
import hristostefanov.minirxdemo.business.User
import io.reactivex.Maybe
import javax.inject.Inject

class PersistedDataSource @Inject constructor(private val database: Database) {
    fun getAllPosts(): Maybe<List<Post>> {
        return database.postDao().getAll()
            .map { list ->
                list.map {
                    Post(title = it.title, userId = it.userId, id = it.id, body = it.body)
                }
            }
    }

    fun getUserById(userId: Int): Maybe<User> {
        return database.userDao().getUserById(userId).map {
            User(it.id, it.username)
        }
    }

    fun savePosts(posts: List<Post>) {
        database.postDao().insert(posts.map {
            PostEntity(it.id, it.title, it.body, it.userId)
        })
    }

    fun saveUser(user: User) {
        database.userDao().insert(UserEntity(user.id, user.username))
    }
}