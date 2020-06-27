package hristostefanov.minirxdemo.utilities.db

import androidx.room.Database
import androidx.room.RoomDatabase
import hristostefanov.minirxdemo.business.gateways.local.PostDAO
import hristostefanov.minirxdemo.business.gateways.local.UserDAO
import hristostefanov.minirxdemo.business.entities.PostEntity
import hristostefanov.minirxdemo.business.entities.UserEntity

@Database(entities = [PostEntity::class, UserEntity::class], version = 1, exportSchema = true)
abstract class Database: RoomDatabase() {
    abstract fun postDao(): PostDAO
    abstract fun userDao(): UserDAO
}