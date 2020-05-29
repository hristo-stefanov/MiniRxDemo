package hristostefanov.minirxdemo.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Observable

@Dao
interface PostDAO {
    @Query("SELECT * FROM post")
    fun getAll(): Observable<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(list: List<PostEntity>)

    @Query("DELETE FROM post")
    fun deleteAll()
}