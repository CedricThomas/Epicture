package com.dev.epicture.epicture.imgur.service.models

data class AlbumModel(
    val id : String?,
    val link: String?,
    val account_url: String?,
    val title: String?,
    val description: String?,
    var selected: Boolean = false,
    var favorite: Boolean?,
    var images: ArrayList<ImageModel>?
)