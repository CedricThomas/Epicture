package com.dev.epicture.epicture.imgur.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.content.ContextCompat.startActivity
import com.dev.epicture.epicture.imgur.service.models.ImageModel
import com.dev.epicture.epicture.imgur.service.models.ImgurFullResponseModel
import com.dev.epicture.epicture.imgur.service.models.ImgurResponseModel
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import com.google.gson.reflect.TypeToken




class ImgurService(val clientId: String, val clientSecret: String) {

    
    private val host = "api.imgur.com"
    private val apiVersion = "3"

    private val informations : HashMap<String, String> = HashMap()
    private val client: OkHttpClient = OkHttpClient.Builder().build()


    fun authorize(context: Context) {
        val url = "https://api.imgur.com/oauth2/authorize?client_id=$clientId&response_type=token"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(context, intent, null)
    }

    fun registerCallbackInformations(intent: Intent) {
        val queryData = intent.dataString!!.split('#')[1]
        for (raw in queryData.split("&")) {
            val pair = raw.split("=")
            informations[pair[0]] = pair[1]
        }
    }

    fun getImages(resolve: (ImgurResponseModel<ImageModel>) -> Unit, reject: (Exception) -> Unit) {

        val url = HttpUrl.Builder()
            .scheme("https")
            .host(host)
            .addPathSegment(apiVersion)
            .addPathSegment("account")
            .addPathSegment("Arzad")
            .addPathSegment("images")
            .addPathSegment("0")
            .build()

        val request = GETBuilder(url)
        val customResolve = { res: ImgurFullResponseModel ->
            val data = Gson().fromJson<ArrayList<ImageModel>>(res.data, object : TypeToken<ArrayList<ImageModel>>() {}.type)
            resolve(ImgurResponseModel(
                data,
                res.success,
                res.status
            ))
        }

        asyncLaunch(request!!, customResolve, reject)
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

    private fun asyncLaunch(request: Request, resolve: (ImgurFullResponseModel) -> Unit, reject: (Exception) -> Unit) {

        client.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                try {
                    val type = object : TypeToken<ImgurFullResponseModel>() {}.type
                    val dataModel = Gson().fromJson<ImgurFullResponseModel>(response.body()!!.string()!!, type)
                    if (dataModel.success) {
                        return resolve(dataModel)
                    }
                   return reject(java.lang.Exception("Invalid response"))
                } catch (e: Exception) {
                    return reject(e)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                reject(e)
            }

        })

    }

}