package com.dev.epicture.epicture.imgur.service.models

data class ImageModel(
    val id : String?,
    val link: String?,
    val title: String?,
    val description: String?,
    var selected: Boolean = false,
    var favorite: Boolean?
)