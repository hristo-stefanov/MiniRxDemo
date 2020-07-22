package hristostefanov.minirxdemo.utilities.db

import androidx.room.Database
import androidx.room.RoomDatabase
import hristostefanov.minirxdemo.business.gateways.local.PostDAO
import hristostefanov.minirxdemo.business.gateways.local.UserDAO
import hristostefanov.minirxdemo.business.entities.Post
import hristostefanov.minirxdemo.business.entities.User

@Database(entities = [Post::class, User::class], version = 1, exportSchema = true)
abstract class Database: RoomDatabase() {
    abstract fun postDao(): PostDAO
    abstract fun userDao(): UserDAO
}