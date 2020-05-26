package hristostefanov.minirxdemo.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Maybe

@Dao
interface PostDAO {
    @Query("SELECT * FROM post")
    fun getAll(): Maybe<List<PostEntity>>

    @Insert
    fun insert(list: List<PostEntity>)
}