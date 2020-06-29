package hristostefanov.minirxdemo.business.entities

import androidx.room.*
import androidx.room.ForeignKey.CASCADE

@Entity(
    tableName = "post",
    // for referential integrity, generates a a FOREIGN KEY constraint
    foreignKeys = [
        ForeignKey(
            childColumns = ["userId"], entity = User::class, parentColumns = ["id"],
            deferred = true,
            onDelete = CASCADE, onUpdate = CASCADE
        )
    ],
    // for faster check of referential integrity
    indices = [Index("userId")]
)
data class Post(
    @PrimaryKey
    val id: Int,
    val title: String,
    val body: String,
    val userId: Int
)