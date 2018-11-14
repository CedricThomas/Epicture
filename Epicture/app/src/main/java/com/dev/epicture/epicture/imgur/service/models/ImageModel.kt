package com.dev.epicture.epicture.imgur.service.models

data class ImageModel(
    val id : String?,
    val link: String?,
    val mp4: String?,
    val title: String?,
    val description: String?,
    var favorite: Boolean?
) : SelectableModel()