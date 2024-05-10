package com.example.desafiogryfo.util

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody


class NetworkUtils {
    fun post (json: String) : String{
        val URL = "https://api.gryfo.com.br/face_match"

        val headerHttp = "application/json; charset=utf-8".toMediaTypeOrNull()

        val client = OkHttpClient()

        val body = json.toRequestBody(headerHttp)

        val request = Request.Builder().url(URL).post(body).build()

        val response = client.newCall(request).execute()

        return response.body.toString()
    }

}