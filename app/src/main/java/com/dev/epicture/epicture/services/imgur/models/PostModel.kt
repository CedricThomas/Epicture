package com.dev.epicture.epicture.services.imgur.models

import com.google.gson.JsonElement

enum class PostType {
    GalleryImage,
    Album
}

data class PostModel(
    val id: String?,
    val title: String?,
    val viewNb: Int?,
    val upNb: Int?,
    val downNb: Int?,
    val previewUrl: String?,
    val mp4Url: String?,
    val fullElement: JsonElement,
    val type : PostType,
    var favorite: Boolean?,
    var vote: String?,
    val is_album: Boolean
) : SelectableModel()