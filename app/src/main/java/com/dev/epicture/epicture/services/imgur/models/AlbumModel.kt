package com.dev.epicture.epicture.services.imgur.models

data class AlbumModel(
    val id : String?,
    val link: String?,
    val views: Int?,
    val ups: Int?,
    val downs: Int?,
    val account_url: String?,
    val title: String?,
    val description: String?,
    var selected: Boolean = false,
    var favorite: Boolean?,
    val vote: String?,
    var images: ArrayList<ImageModel>?
)