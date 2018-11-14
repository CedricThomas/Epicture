package com.dev.epicture.epicture.imgur.service.models

data class GalleryImageModel(
    val id : String?,
    val link: String?,
    val mp4: String?,
    val account_url: String?,
    val title: String?,
    val description: String?,
    var selected: Boolean = false,
    var favorite: Boolean?,
    var images: ArrayList<ImageModel>?
)