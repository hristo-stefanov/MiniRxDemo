package hristostefanov.minirxdemo.business.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "user")
data class User(
    @PrimaryKey
    val id: Int,
    val username: String
)