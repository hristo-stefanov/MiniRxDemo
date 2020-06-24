package hristostefanov.minirxdemo.business

interface UserGateway {
    fun insert(list: List<User>)
    fun deleteAll()
}