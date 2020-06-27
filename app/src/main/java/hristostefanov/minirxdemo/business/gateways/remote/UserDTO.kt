package hristostefanov.minirxdemo.business.gateways.remote

import com.google.gson.annotations.SerializedName

data class UserDTO(
    @SerializedName("id")
    val id: Int,
    @SerializedName("username")
    val username: String
)