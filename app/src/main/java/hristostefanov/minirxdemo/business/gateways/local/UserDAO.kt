package hristostefanov.minirxdemo.business.gateways.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import hristostefanov.minirxdemo.business.entities.UserEntity

@Dao
interface UserDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entities: List<UserEntity>)

    @Query("DELETE FROM user")
    fun deleteAll()
}