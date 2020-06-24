package hristostefanov.minirxdemo.persistence

import hristostefanov.minirxdemo.business.User
import hristostefanov.minirxdemo.business.UserGateway
import javax.inject.Inject

class UserGatewayImpl @Inject constructor(private val database: Database): UserGateway {
    override fun insert(list: List<User>) {
        database.userDao().insert(list.map {
            UserEntity(it.id, it.username)
        })
    }

    override fun deleteAll() {
        database.userDao().deleteAll()
    }

}