package com.dev.epicture.epicture.services.imgur.models

/**
 * Template class allowing to receive every ImgurAPI response
 */
data class BasicImgurResponseModel<T> (
    val data : T,
    val success: Boolean,
    val status : Int
)