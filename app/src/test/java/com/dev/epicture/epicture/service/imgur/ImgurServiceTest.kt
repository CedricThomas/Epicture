package com.dev.epicture.epicture.service.imgur

import android.graphics.Bitmap
import com.dev.epicture.epicture.services.imgur.ImgurService
import org.junit.Assert.assertTrue
import org.junit.Test

class ImgurServiceTest {

    @Test
    fun getImagesAuthenticatedTest() {
        var auth = false
        try {
            ImgurService.getImages({}, {})
        } catch (e: Exception) {
            auth = true
        }
        assertTrue("getImages is not authenticated", auth)
    }

    @Test
    fun getGalleryAuthenticatedTest() {
        var auth = false
        try {
            ImgurService.getGallery({}, {})
        } catch (e: Exception) {
            auth = true
        }
        assertTrue("getGallery is not authenticated", auth)
    }

    @Test
    fun getAvatarAuthenticatedTest() {
        var auth = false
        try {
            ImgurService.getAvatar({}, {})
        } catch (e: Exception) {
            auth = true
        }
        assertTrue("getAvatar is not authenticated", auth)
    }

    @Test
    fun voteAuthenticatedTest() {
        var auth = false
        try {
            ImgurService.vote({}, {}, "ok", "up")
        } catch (e: Exception) {
            auth = true
        }
        assertTrue("vote is not authenticated", auth)
    }

    @Test
    fun getFavoriteAuthenticatedTest() {
        var auth = false
        try {
            ImgurService.getFavorite({}, {})
        } catch (e: Exception) {
            auth = true
        }
        assertTrue("getFavorite is not authenticated", auth)
    }

    @Test
    fun searchAuthenticatedTest() {
        var auth = false
        try {
            ImgurService.search({}, {}, "cats")
        } catch (e: Exception) {
            auth = true
        }
        assertTrue("search is not authenticated", auth)
    }

    @Test
    fun deleteImageAuthenticatedTest() {
        var auth = false
        try {
            ImgurService.deleteImage({}, {}, "id")
        } catch (e: Exception) {
            auth = true
        }
        assertTrue("deleteImage is not authenticated", auth)
    }

    @Test
    fun favoriteImageAuthenticatedTest() {
        var auth = false
        try {
            ImgurService.favoriteImage({}, {}, "id")
        } catch (e: Exception) {
            auth = true
        }
        assertTrue("favoriteImage is not authenticated", auth)
    }

    @Test
    fun favoriteAlbumAuthenticatedTest() {
        var auth = false
        try {
            ImgurService.favoriteAlbum({}, {}, "id")
        } catch (e: Exception) {
            auth = true
        }
        assertTrue("favoriteAlbum is not authenticated", auth)
    }

    @Test
    fun uploadImageAuthenticatedTest() {
        var auth = false
        try {
            val bitmap: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8)
            ImgurService.uploadImage({}, {},
                "id",
                "ok",
                "ok",
                bitmap)
        } catch (e: Exception) {
            auth = true
        }
        assertTrue("uploadImage is not authenticated", auth)
    }
}