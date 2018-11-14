package com.dev.epicture.epicture.imgur.service.models

import com.google.gson.JsonElement

enum class PostType {
    GalleryImage,
    Album
}

data class PostModel(
    val title: String?,
    val viewNb: Int?,
    val upNb: Int?,
    val downNb: Int?,
    val previewUrl: String?,
    val mp4Url: String?,
    val fullElement: JsonElement,
    val type : PostType
) : SelectableModel()