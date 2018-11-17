package com.dev.epicture.epicture.services.imgur

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat.startActivity
import android.util.Log
import com.dev.epicture.epicture.MyApplication
import com.dev.epicture.epicture.services.imgur.models.AvatarModel
import com.dev.epicture.epicture.services.imgur.models.BasicImgurResponseModel
import com.dev.epicture.epicture.services.imgur.models.ImageModel
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.IOException


object ImgurService {

    private val prefKeys: List<String> = listOf(
        "account_username",
        "account_id",
        "refresh_token"
    )

    private const val clientId: String = Config.clientID
    private const val clientSecret: String = Config.clientSecret

    private var authenticated = false

    private val host = "api.imgur.com"
    private val apiVersion = "3"

    private val informations : HashMap<String, String> = HashMap()
    private val client: OkHttpClient = OkHttpClient.Builder().build()



    // Credentials zone

    fun askCredentials(context: Context) {
        val url = "https://api.imgur.com/oauth2/authorize?client_id=$clientId&response_type=token"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(context, intent, null)
    }

    fun deleteCredentials() {
        val editor= PreferenceManager.getDefaultSharedPreferences(MyApplication.appContext).edit()
        editor.clear().apply()
    }

    fun loadCredentials(resolve: () -> Unit, reject: () -> Unit) {
        if (!loadFromPrefs())
            return reject()
        refreshCredentials(resolve, reject)
    }

    private fun loadFromPrefs(): Boolean {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.appContext)
        for (key in prefKeys) {
            val pref = prefs.getString(key, "")!!
            if (pref.isEmpty()) {
                informations.clear()
                return false
            }
            informations[key] = pref
        }
        return true
    }

    private fun saveInPrefs() {
        val editor= PreferenceManager.getDefaultSharedPreferences(MyApplication.appContext).edit()
        for (key in prefKeys) {
            editor.putString(key, informations[key])
        }
        editor.apply()
    }

    fun registerCredentials(intent: Intent){
        val queryData = intent.dataString!!.split('#')[1]
        for (raw in queryData.split("&")) {
            val pair = raw.split("=")
            informations[pair[0]] = pair[1]
        }
        saveInPrefs()
        authenticated = true
    }

    private fun refreshCredentials(resolve: () -> Unit, reject: () -> Unit) {

        val url = HttpUrl.Builder()
            .scheme("https")
            .host(host)
            .addPathSegment("oauth2")
            .addPathSegment("token")
            .build()

        val body = FormBody.Builder()
            .add("refresh_token", informations["refresh_token"]!!)
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .add("grant_type", "refresh_token")
            .build()

        val request = Request.Builder()
            .url(url)
            .post(body)
            .header("Authorization", "Bearer $clientId")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                val data = Gson().fromJson<JsonElement>(response.body()!!.string()!!, JsonElement::class.java)
                for (elem in listOf("access_token", "refresh_token", "expires_in")) {
                    informations[elem] = data.asJsonObject[elem].asString
                }
                saveInPrefs()
                authenticated = true
                return resolve()
            }

            override fun onFailure(call: Call, e: IOException) {
                return reject()
            }

        })
    }

    // vote
    fun vote(
        resolve: (JsonElement) -> Unit,
        reject: (Exception) -> Unit,
        id: String,
        action: String
    ) {
        if (!authenticated)
            throw IOException("You are not connected")

        Log.i("ImgurService", "vote")
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(host)
            .addPathSegment(apiVersion)
            .addPathSegment("gallery")
            .addPathSegment(id)
            .addPathSegment("vote")
            .addPathSegment(action)
            .build()

        val body = RequestBody.create(null, "")

        val request = POSTBuilder(url, body)
        asyncLaunch(request!!, resolve, reject)
    }

    // get Images
    fun getImages(resolve: (BasicImgurResponseModel<ArrayList<ImageModel>>) -> Unit, reject: (Exception) -> Unit, page: String = "") {
        if (!authenticated)
            throw IOException("You are not connected")

        Log.i("ImgurService", "getImages")
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(host)
            .addPathSegment(apiVersion)
            .addPathSegment("account")
            .addPathSegment(informations["account_username"]!!)
            .addPathSegment("images")
            .addPathSegment(page)
            .build()

        val request = GETBuilder(url)
        val customResolve = { res: JsonElement ->
            val type = object : TypeToken<BasicImgurResponseModel<ArrayList<ImageModel>>>() {}.type
            val data = Gson().fromJson<BasicImgurResponseModel<ArrayList<ImageModel>>>(res.toString(), type)
            resolve(data)
        }
        asyncLaunch(request!!, customResolve, reject)
    }

    // get Favorite
    fun getFavorite(resolve: (BasicImgurResponseModel<ArrayList<JsonElement>>) -> Unit, reject: (Exception) -> Unit, page: String = "") {
        if (!authenticated)
            throw IOException("You are not connected")

        Log.i("ImgurService", "getFavorite")
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(host)
            .addPathSegment(apiVersion)
            .addPathSegment("account")
            .addPathSegment(informations["account_username"]!!)
            .addPathSegment("favorites")
            .addPathSegment(page)
            .build()

        val request = GETBuilder(url)
        val customResolve = { res: JsonElement ->
            val type = object : TypeToken<BasicImgurResponseModel<ArrayList<JsonElement>>>() {}.type
            val data = Gson().fromJson<BasicImgurResponseModel<ArrayList<JsonElement>>>(res.toString(), type)
            resolve(data)
        }
        asyncLaunch(request!!, customResolve, reject)
    }

    // Search
    fun search(resolve: (BasicImgurResponseModel<ArrayList<JsonElement>>) -> Unit, reject: (Exception) -> Unit, query: String,
               page: String = "0", sort: String = "time", window: String = "all") {
        if (!authenticated)
            throw IOException("You are not connected")

        Log.i("ImgurService", "search")
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(host)
            .addPathSegment(apiVersion)
            .addPathSegment("gallery")
            .addPathSegment("search")
            .addPathSegment(sort)
            .addPathSegment(window)
            .addPathSegment(page)
            .addQueryParameter("q", query)
            .build()

        val request = GETBuilder(url)
        val customResolve = { res: JsonElement ->
            val type = object : TypeToken<BasicImgurResponseModel<ArrayList<JsonElement>>>() {}.type
            val data = Gson().fromJson<BasicImgurResponseModel<ArrayList<JsonElement>>>(res.toString(), type)
            resolve(data)
        }
        asyncLaunch(request!!, customResolve, reject)
    }

    // get a gallery
    fun getGallery(resolve: (BasicImgurResponseModel<ArrayList<JsonElement>>) -> Unit, reject: (Exception) -> Unit,
                  page: String = "0", section: String = "hot", sort: String = "viral", window: String = "day") {
        if (!authenticated)
            throw IOException("You are not connected")

        Log.i("ImgurService", "getGallery")

        val url = HttpUrl.Builder()
            .scheme("https")
            .host(host)
            .addPathSegment(apiVersion)
            .addPathSegment("gallery")
            .addPathSegment(section)
            .addPathSegment(sort)
            .addPathSegment(window)
            .addPathSegment(page)
            .build()

        val request = GETBuilder(url)
        val customResolve = { res: JsonElement ->
            val type = object : TypeToken<BasicImgurResponseModel<ArrayList<JsonElement>>>() {}.type
            val data = Gson().fromJson<BasicImgurResponseModel<ArrayList<JsonElement>>>(res.toString(), type)
            resolve(data)
        }
        asyncLaunch(request!!, customResolve, reject)
    }

    // get custom AvatarModel
    fun getAvatar(resolve: (BasicImgurResponseModel<AvatarModel>) -> Unit, reject: (Exception) -> Unit) {
        if (!authenticated)
            throw IOException("You are not connected")

        Log.i("ImgurService", "getAvatar")
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(host)
            .addPathSegment(apiVersion)
            .addPathSegment("account")
            .addPathSegment(informations["account_username"]!!)
            .addPathSegment("avatar")
            .build()

        val request = GETBuilder(url)
        val customResolve = { res: JsonElement ->
            try {
                val type = object : TypeToken<BasicImgurResponseModel<AvatarModel>>() {}.type
                val data = Gson().fromJson<BasicImgurResponseModel<AvatarModel>>(res.toString(), type)
                val model = AvatarModel(data.data.avatar, informations["account_username"])
                resolve(BasicImgurResponseModel(model, data.success, data.status))
            } catch (e: java.lang.Exception) {
                reject(e)
            }
        }
        asyncLaunch(request!!, customResolve, reject)
    }

    // delete User Image
    fun deleteImage(resolve: (JsonElement) -> Unit, reject: (Exception) -> Unit, id: String) {
        if (!authenticated)
            throw IOException("You are not connected")

        Log.i("ImgurService", "deleteImage")
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(host)
            .addPathSegment(apiVersion)
            .addPathSegment("image")
            .addPathSegment(id)
            .build()

        val request = DELETEBuilder(url)
        asyncLaunch(request!!, resolve, reject)
    }

    // toggle Favorite on Image
    fun favoriteImage(
        resolve: (JsonElement) -> Unit,
        reject: (Exception) -> Unit,
        id: String
    ) {
        if (!authenticated)
            throw IOException("You are not connected")

        Log.i("ImgurService", "favoriteImage")
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(host)
            .addPathSegment(apiVersion)
            .addPathSegment("image")
            .addPathSegment(id)
            .addPathSegment("favorite")
            .build()

        val body = RequestBody.create(null, "")

        val request = POSTBuilder(url, body)
        asyncLaunch(request!!, resolve, reject)
    }

    // toggle Favorite on Album
    fun favoriteAlbum(
        resolve: (JsonElement) -> Unit,
        reject: (Exception) -> Unit,
        id: String
    ) {
        if (!authenticated)
            throw IOException("You are not connected")

        Log.i("ImgurService", "favoriteAlbum")
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(host)
            .addPathSegment(apiVersion)
            .addPathSegment("album")
            .addPathSegment(id)
            .addPathSegment("favorite")
            .build()

        val body = RequestBody.create(null, "")

        val request = POSTBuilder(url, body)
        asyncLaunch(request!!, resolve, reject)
    }

    // upload a Bitmap (Async)
    fun uploadImage(
        resolve: (JsonElement) -> Unit,
        reject: (Exception) -> Unit,
        name: String,
        title: String,
        description: String,
        image: Bitmap
    ) {
        if (!authenticated)
            throw IOException("You are not connected")

        Log.i("ImgurService", "uploadImage")
        Thread(Runnable {
            val url = HttpUrl.Builder()
                .scheme("https")
                .host(host)
                .addPathSegment(apiVersion)
                .addPathSegment("image")
                .build()

            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "image.jpg",
                    bitmapToByteArray(image)
                )
                .addFormDataPart("title", title)
                .addFormDataPart("description", description)
                .addFormDataPart("name", name)
                .build()

            val request = POSTBuilder(url, body)
            asyncLaunch(request!!, resolve, reject)

        }).start()
    }

    // Tools

    private fun POSTBuilder(url: HttpUrl, body: RequestBody): Request? {
        return Request.Builder()
            .url(url)
            .header("Authorization", "Client-ID $clientId")
            .header("Authorization", "Bearer ${informations["access_token"]}")
            .header("User-Agent", "Epicture")
            .post(body)
            .build()
    }

    private fun GETBuilder(url: HttpUrl): Request? {
        return Request.Builder()
            .url(url)
            .header("Authorization", "Client-ID $clientId")
            .header("Authorization", "Bearer ${informations["access_token"]}")
            .header("User-Agent", "Epicture")
            .get()
            .build()
    }

    private fun DELETEBuilder(url: HttpUrl): Request? {
        return Request.Builder()
            .url(url)
            .header("Authorization", "Client-ID $clientId")
            .header("Authorization", "Bearer ${informations["access_token"]}")
            .header("User-Agent", "Epicture")
            .delete()
            .build()
    }

    private fun bitmapToByteArray(bmp: Bitmap): RequestBody {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return RequestBody.create(MediaType.parse("image/*jpg"), byteArrayOutputStream.toByteArray())

    }

    private fun asyncLaunch(request: Request, resolve: (JsonElement) -> Unit, reject: (Exception) -> Unit) {

        client.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                try {
                    val data = Gson().fromJson<JsonElement>(response.body()!!.string()!!, JsonElement::class.java)
                    val dataModel = data.asJsonObject
                    if (dataModel.get("success").asBoolean)
                        return resolve(data)
                    return reject(java.lang.Exception("Invalid response : $response"))
                } catch (e: Exception) {
                    return reject(e)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                return reject(e)
            }

        })

    }

}