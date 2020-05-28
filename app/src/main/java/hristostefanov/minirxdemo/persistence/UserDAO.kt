package hristostefanov.minirxdemo.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface UserDAO {
    @Query("SELECT * FROM user WHERE id = :userId")
    fun getUserById(userId: Int): Single<UserEntity>

    @Insert
    fun insert(userEntity: UserEntity)

    @Insert
    fun insert(entities: List<UserEntity>)

    @Query("DELETE FROM user")
    fun deleteAll()
}