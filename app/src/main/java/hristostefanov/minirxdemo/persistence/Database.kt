package hristostefanov.minirxdemo.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import hristostefanov.minirxdemo.business.Post

@Database(entities = [PostEntity::class, UserEntity::class], version = 1, exportSchema = true)
abstract class Database: RoomDatabase() {
    abstract fun postDao(): PostDAO
    abstract fun userDao(): UserDAO
}