package com.dev.epicture.epicture.imgur.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.content.ContextCompat.startActivity
import android.util.Log
import com.dev.epicture.epicture.imgur.service.models.BasicImgurResponseModel
import com.dev.epicture.epicture.imgur.service.models.ImageModel
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException
import org.json.JSONObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.HttpUrl
import android.graphics.Bitmap
import android.R.attr.bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream


object ImgurService {

    private const val clientId: String = Config.clientID
    private const val clientSecret: String = Config.clientSecret

    private val host = "api.imgur.com"
    private val apiVersion = "3"

    private val informations : HashMap<String, String> = HashMap()
    private val client: OkHttpClient = OkHttpClient.Builder().build()

    fun authorize(context: Context) {
        val url = "https://api.imgur.com/oauth2/authorize?client_id=$clientId&response_type=token"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(context, intent, null)
    }

    fun registerCallbackInformations(intent: Intent){
        val queryData = intent.dataString!!.split('#')[1]
        for (raw in queryData.split("&")) {
            val pair = raw.split("=")
            informations[pair[0]] = pair[1]
        }
    }

    fun getImages(resolve: (BasicImgurResponseModel<ArrayList<ImageModel>>) -> Unit, reject: (Exception) -> Unit, page: String = "") {
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

    fun deleteImage(resolve: (JsonElement) -> Unit, reject: (Exception) -> Unit, id: String) {
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

    fun uploadImage(
        resolve: (JsonElement) -> Unit,
        reject: (Exception) -> Unit,
        name: String,
        title: String,
        description: String,
        image: Bitmap
    ) {

        val url = HttpUrl.Builder()
            .scheme("https")
            .host(host)
            .addPathSegment(apiVersion)
            .addPathSegment("image")
            .build()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", bitmapToBase64(image)!!)
            .addFormDataPart("title", title)
            .addFormDataPart("description", description)
            .addFormDataPart("name", name)
            .build()

        val request = POSTBuilder(url, body)
        return asyncLaunch(request!!, resolve, reject)
    }

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

    private fun bitmapToBase64(bmp: Bitmap): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun asyncLaunch(request: Request, resolve: (JsonElement) -> Unit, reject: (Exception) -> Unit) {

        client.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                try {
                    val data = Gson().fromJson<JsonElement>(response.body()!!.string()!!, JsonElement::class.java)
                    val dataModel = data.asJsonObject
                    if (dataModel.get("success").asBoolean)
                        return resolve(data)
                    return reject(java.lang.Exception("Invalid response"))
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