package com.dev.epicture.epicture.services.imgur.models

import com.google.gson.Gson
import com.google.gson.JsonElement

enum class PostType {
    GalleryImage,
    Album
}

data class PostModel(
    val id: String?,
    val title: String?,
    val viewNb: Int?,
    var upNb: Int?,
    var downNb: Int?,
    val previewUrl: String?,
    val mp4Url: String?,
    val fullElement: JsonElement,
    val type : PostType,
    var favorite: Boolean?,
    var vote: String?,
    val is_album: Boolean
) : SelectableModel()


// Convert JsonElement to PostModel using AlbumModel and GalleryImageModel

class PostUtils {

    companion object {

        fun elementToPost(element: JsonElement): PostModel {
            val gson = Gson()
            if (element.asJsonObject.has("images")) {
                val data : AlbumModel = gson.fromJson(element, AlbumModel::class.java)
                // Album
                return PostModel(
                    data.id,
                    data.title,
                    data.views,
                    data.ups,
                    data.downs,
                    data.images?.get(0)?.link,
                    data.images?.get(0)?.mp4,
                    element,
                    PostType.Album,
                    data.favorite,
                    data.vote,
                    element.asJsonObject.get("is_album").asBoolean
                )
            } else {
                val data : GalleryImageModel = gson.fromJson(element, GalleryImageModel::class.java)
                // Gallery Image
                return PostModel(
                    data.id,
                    data.title,
                    data.views,
                    data.ups,
                    data.downs,
                    data.link,
                    data.mp4,
                    element,
                    PostType.GalleryImage,
                    data.favorite,
                    data.vote,
                    element.asJsonObject.get("is_album").asBoolean
                )
            }
        }
    }
}
