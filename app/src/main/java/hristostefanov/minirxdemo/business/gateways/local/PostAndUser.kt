package hristostefanov.minirxdemo.business.gateways.local

import androidx.room.Embedded
import androidx.room.Relation
import hristostefanov.minirxdemo.business.entities.Post
import hristostefanov.minirxdemo.business.entities.User

data class PostAndUser(
    @Embedded
    val post: Post,

    // NOTE: to get *all* posts with related users, we pretend "post" is the parent table in the
    // relation, while in fact the parent table is "user"
    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    val user: User
)