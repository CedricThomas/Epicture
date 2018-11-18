package com.dev.epicture.epicture.service.imgur.model

import com.dev.epicture.epicture.services.imgur.models.PostType
import com.dev.epicture.epicture.services.imgur.models.PostUtils
import com.google.gson.JsonParser
import org.junit.Test
import org.junit.Assert.*

class PostModelTest {

    @Test
    fun albumJsonElementToPostModelTest() {
        val elem = """
            {
                "id": "id_value",
                "title" : "title_value",
                "views" : "10",
                "ups" : "42",
                "downs" : "-42",
                "images" : [
                    {
                        "link" : "image_link_value",
                        "mp4" : "image_mp4_value"
                    }
                ],
                "favorite" : true,
                "vote": "up",
                "is_album": false
            }
        """

        val response = JsonParser().parse(elem)
        val post = PostUtils.elementToPost(response)
        assertEquals("invalid id", post.id, "id_value")
        assertEquals("invalid title", post.title, "title_value")
        assertEquals("invalid view", post.viewNb, 10)
        assertEquals("invalid ups", post.upNb, 42)
        assertEquals("invalid downs", post.downNb, -42)
        assertEquals("invalid previewUrl", post.previewUrl, "image_link_value")
        assertEquals("invalid mp4Url", post.mp4Url, "image_mp4_value")
        assertEquals("invalid favorite", post.favorite, true)
        assertEquals("invalid vote", post.vote, "up")
        assertEquals("invalid is_album", post.is_album, false)
        assertEquals("invalid type", post.type, PostType.Album)
        assertEquals("invalid original element", post.fullElement, response)
    }

    @Test
    fun imageJsonElementToPostModelTest() {
        val elem = """
            {
                "id": "id_value",
                "title" : "title_value",
                "views" : "10",
                "ups" : "42",
                "downs" : "-42",
                "link" : "image_link_value",
                "mp4" : "image_mp4_value",
                "favorite" : true,
                "vote": "up",
                "is_album": false
            }
        """

        val response = JsonParser().parse(elem)
        val post = PostUtils.elementToPost(response)
        assertEquals("invalid id", post.id, "id_value")
        assertEquals("invalid title", post.title, "title_value")
        assertEquals("invalid view", post.viewNb, 10)
        assertEquals("invalid ups", post.upNb, 42)
        assertEquals("invalid downs", post.downNb, -42)
        assertEquals("invalid previewUrl", post.previewUrl, "image_link_value")
        assertEquals("invalid mp4Url", post.mp4Url, "image_mp4_value")
        assertEquals("invalid favorite", post.favorite, true)
        assertEquals("invalid vote", post.vote, "up")
        assertEquals("invalid is_album", post.is_album, false)
        assertEquals("invalid type", post.type, PostType.GalleryImage)
        assertEquals("invalid original element", post.fullElement, response)
    }

}