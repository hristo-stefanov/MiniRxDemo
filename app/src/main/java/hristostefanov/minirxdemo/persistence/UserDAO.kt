package hristostefanov.minirxdemo.persistence

import androidx.room.*
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface UserDAO {
    @Query("SELECT * FROM user WHERE id = :userId")
    fun getUserById(userId: Int): Single<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userEntity: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entities: List<UserEntity>)

    @Query("DELETE FROM user")
    fun deleteAll()
}