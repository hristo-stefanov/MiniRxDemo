package hristostefanov.minirxdemo.business.gateways.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import hristostefanov.minirxdemo.business.entities.User

@Dao
interface UserDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entities: List<User>)

    @Query("DELETE FROM user")
    fun deleteAll()
}