package hristostefanov.minirxdemo.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Maybe

@Dao
interface UserDAO {
    @Query("SELECT * FROM user WHERE id = :userId")
    fun getUserById(userId: Int): Maybe<UserEntity>
    @Insert
    fun insert(userEntity: UserEntity)
}