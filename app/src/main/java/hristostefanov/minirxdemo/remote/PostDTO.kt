package hristostefanov.minirxdemo.remote

import com.google.gson.annotations.SerializedName
// TODO this does not look like using the DTD pattern at all, call it Remote, external or without any suffix
// because it could be auto-generated
data class PostDTO(
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("body")
    val body: String
)