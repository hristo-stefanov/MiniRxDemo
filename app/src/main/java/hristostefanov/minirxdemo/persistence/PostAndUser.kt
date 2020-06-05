package hristostefanov.minirxdemo.persistence

import androidx.room.Embedded
import androidx.room.Relation

data class PostAndUser(
    @Embedded
    val post: PostEntity,

    // NOTE: to get *all* posts with related users, we pretend "post" is the parent table in the
    // relation, while in fact the parent table is "user"
    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    val user: UserEntity
)