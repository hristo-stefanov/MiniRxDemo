package hristostefanov.minirxdemo.business.gateways.local

import androidx.room.*
import hristostefanov.minirxdemo.business.entities.Post
import io.reactivex.Observable

@Dao
interface PostDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(list: List<Post>)

    @Query("DELETE FROM post")
    fun deleteAll()

    @Query("SELECT * FROM post ORDER BY title")
    @Transaction
    fun observePostAndUserSortedByTitleInTx(): Observable<List<PostAndUser>>
}