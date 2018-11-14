package com.dev.epicture.epicture.imgur.service.models

data class BasicImgurResponseModel<T> (
    val data : T,
    val success: Boolean,
    val status : Int
)