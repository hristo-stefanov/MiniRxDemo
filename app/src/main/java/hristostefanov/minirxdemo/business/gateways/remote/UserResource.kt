package hristostefanov.minirxdemo.business.gateways.remote

import com.google.gson.annotations.SerializedName

data class UserResource(
    @SerializedName("id")
    val id: Int,
    @SerializedName("username")
    val username: String
)