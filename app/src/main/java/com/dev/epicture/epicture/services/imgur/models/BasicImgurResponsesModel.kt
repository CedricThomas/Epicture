package com.dev.epicture.epicture.services.imgur.models

data class BasicImgurResponseModel<T> (
    val data : T,
    val success: Boolean,
    val status : Int
)