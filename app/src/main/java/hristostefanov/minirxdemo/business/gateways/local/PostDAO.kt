package hristostefanov.minirxdemo.business.gateways.local

import androidx.room.*
import hristostefanov.minirxdemo.business.entities.PostEntity
import io.reactivex.Observable

@Dao
interface PostDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(list: List<PostEntity>)

    @Query("DELETE FROM post")
    fun deleteAll()

    @Query("SELECT * FROM post")
    @Transaction
    fun getPostAndUserInTx(): Observable<List<PostAndUser>>
}