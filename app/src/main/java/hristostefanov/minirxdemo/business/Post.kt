package hristostefanov.minirxdemo.business

data class Post(
    val id: Int,
    val title: String,
    val body: String,
    val user: User
)