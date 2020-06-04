package hristostefanov.minirxdemo.business

data class Post(
    val id: Int,
    val title: String,
    val body: String,
    // TODO could be a class association
    val userId: Int
)