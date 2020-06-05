package hristostefanov.minirxdemo.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entities: List<UserEntity>)

    @Query("DELETE FROM user")
    fun deleteAll()
}