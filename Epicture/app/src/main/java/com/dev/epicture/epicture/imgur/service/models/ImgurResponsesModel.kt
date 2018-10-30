package com.dev.epicture.epicture.imgur.service.models

import com.google.gson.JsonArray

data class ImgurFullResponseModel (
    val data : JsonArray,
    val success: Boolean,
    val status : Int
)

data class ImgurResponseModel<T> (
    val data : ArrayList<T>,
    val success: Boolean,
    val status : Int
)