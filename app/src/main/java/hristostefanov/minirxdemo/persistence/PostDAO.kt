package hristostefanov.minirxdemo.persistence

import androidx.room.*
import io.reactivex.Observable

@Dao
interface PostDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(list: List<PostEntity>)

    @Query("DELETE FROM post")
    fun deleteAll()

    @Query("SELECT * FROM post")
    @Transaction
    fun getPostAndUser(): Observable<List<PostAndUser>>
}