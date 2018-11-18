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
    /**
     * Start a web asking for imgur Credentials
     * @param context: Base context for the login Intent
     */
    fun askCredentials(context: Context) {
        val url = "https://api.imgur.com/oauth2/authorize?client_id=$clientId&response_type=token"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(context, intent, null)
    }

    /**
     * Remove credential from prefKeys and disable auto login
     */
    fun deleteCredentials() {
        val editor= PreferenceManager.getDefaultSharedPreferences(MyApplication.appContext).edit()
        editor.clear().apply()
    }

    /**
     * Try to load credentials from prefs and refresh token on success (authenticated on success)
     * @param resolve: resolve callback on success
     * @param reject: reject callback on failure
     */
    fun loadCredentials(resolve: () -> Unit, reject: () -> Unit) {
        if (!loadFromPrefs())
            return reject()
        refreshCredentials(resolve, reject)
    }

    /**
     * Try to load credentials from prefs and store all the mandatory keys in variable informations
     *
     * @return: true on success, false on failure
     */
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

    /**
     * Save needed keys of informations in prefKeys
     */
    private fun saveInPrefs() {
        val editor= PreferenceManager.getDefaultSharedPreferences(MyApplication.appContext).edit()
        for (key in prefKeys) {
            editor.putString(key, informations[key])
        }
        editor.apply()
    }

    /**
     * Load intent result in informations and save credentials in prefKeys (authenticated on success)
     */
    fun registerCredentials(intent: Intent){
        val queryData = intent.dataString!!.split('#')[1]
        for (raw in queryData.split("&")) {
            val pair = raw.split("=")
            informations[pair[0]] = pair[1]
        }
        saveInPrefs()
        authenticated = true
    }

    /**
     * refresh credentials on Imgur
     * @param reject: failure callback
     * @param resolve: success callback
     */
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


    /**
     * vote for a post on Imgur (authenticated method)
     * @param reject: failure callback => (JsonElement : success response)
     * @param resolve: success callback => (Exception: call exception)
     * @param id: post id to vote for
     * @param action: vote status
     */
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

    /**
     * Get logged user personnal images on Imgur (authenticated method)
     * @param reject: failure callback => (BasicImgurResponseModel : success response)
     * @param resolve: success callback => (Exception: call exception)
     * @param page: page id of the images gallery to be loaded
     */
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

    /**
     * Get logged user favorites post on Imgur (authenticated method)
     * @param reject: failure callback => (BasicImgurResponseModel : success response)
     * @param resolve: success callback => (Exception: call exception)
     * @param page: page id of the images gallery to be loaded
     */
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

    /**
     * search for post post on Imgur (authenticated method)
     * @param reject: failure callback => (BasicImgurResponseModel : success response)
     * @param resolve: success callback => (Exception: call exception)
     * @param page: page id of the images gallery to be loaded
     * @param query: imgur search query
     * @param sort: sort of the results (default: time) (options : viral | top | time | rising)
     * @param window: window/scope of the results (default: all) (options : day | week | month | year | all)
     */
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

    /**
     * Get gallery posts on Imgur (authenticated method)
     * @param reject: failure callback => (BasicImgurResponseModel : success response)
     * @param resolve: success callback => (Exception: call exception)
     * @param page: page id of the images gallery to be loaded
     * @param section: section of the results (default: hot) (options : hot | top | user)
     * @param sort: sort of the results (default: viral) (options : viral | top | time | rising)
     * @param window: window/scope of the results (default: day) (options : day | week | month | year | all)
     */
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


    /**
     * Get logged user avatar on Imgur (authenticated method)
     * @param reject: failure callback => (BasicImgurResponseModel : success response)
     * @param resolve: success callback => (Exception: call exception)
     */
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


    /**
     * Delete logged user images on Imgur (authenticated method)
     * @param reject: failure callback => (BasicImgurResponseModel : success response)
     * @param resolve: success callback => (Exception: call exception)
     * @param id: id of the image to be removed
     */
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

    /**
     * Favorite an image for the logged user (authenticated method)
     * @param reject: failure callback => (BasicImgurResponseModel : success response)
     * @param resolve: success callback => (Exception: call exception)
     * @param id: id of the image to be fav/unfav
     */
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


    /**
     * Favorite an album for the logged user (authenticated method)
     * @param reject: failure callback => (BasicImgurResponseModel : success response)
     * @param resolve: success callback => (Exception: call exception)
     * @param id: id of the album to be fav/unfav
     */
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


    /**
     * Uplaod an image for the logged user (authenticated method) (async method)
     * @param reject: failure callback => (BasicImgurResponseModel : success response)
     * @param resolve: success callback => (Exception: call exception)
     * @param title: title of the image to be upload
     * @param description: description of the image to be upload
     * @param name: name of the image to be upload
     * @param image: Bitmap of the image to be upload
     */
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
                    bitmapToBody(image)
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

    /**
     * Build a post request from url and body
     * @param url: request url
     * @param body: request body
     */
    private fun POSTBuilder(url: HttpUrl, body: RequestBody): Request? {
        return Request.Builder()
            .url(url)
            .header("Authorization", "Client-ID $clientId")
            .header("Authorization", "Bearer ${informations["access_token"]}")
            .header("User-Agent", "Epicture")
            .post(body)
            .build()
    }

    /**
     * Build a get request from url
     * @param url: request url
     */
    private fun GETBuilder(url: HttpUrl): Request? {
        return Request.Builder()
            .url(url)
            .header("Authorization", "Client-ID $clientId")
            .header("Authorization", "Bearer ${informations["access_token"]}")
            .header("User-Agent", "Epicture")
            .get()
            .build()
    }

    /**
     * Build a delete request from url
     * @param url: request url
     */
    private fun DELETEBuilder(url: HttpUrl): Request? {
        return Request.Builder()
            .url(url)
            .header("Authorization", "Client-ID $clientId")
            .header("Authorization", "Bearer ${informations["access_token"]}")
            .header("User-Agent", "Epicture")
            .delete()
            .build()
    }

    /**
     * Convert an image in request body
     * @param bmp: image to put in boy
     * @return request body for an image upload
     */
    private fun bitmapToBody(bmp: Bitmap): RequestBody {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return RequestBody.create(MediaType.parse("image/*jpg"), byteArrayOutputStream.toByteArray())
    }

    /**
     * Launch an async okhttp request and call the callback associated with the request status
     * @param reject: failure callback
     * @param request: success callback
     */
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