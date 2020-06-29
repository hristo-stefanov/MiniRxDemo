package hristostefanov.minirxdemo.business.gateways.remote

import com.google.gson.annotations.SerializedName
data class PostResource(
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("body")
    val body: String
)