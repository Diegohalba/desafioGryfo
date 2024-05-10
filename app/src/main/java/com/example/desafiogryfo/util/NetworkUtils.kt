package com.example.desafiogryfo.util

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response


class NetworkUtils {
    fun post (json: String) : Response {

        val client = OkHttpClient()
        val URL = "https://api.gryfo.com.br/face_match"

        val headerHttp = "application/json; charset=utf-8".toMediaTypeOrNull()

        val body = RequestBody.create(headerHttp, json)

        val request = Request.Builder()
            .url(URL)
            .post(body)
            .build()

        return client.newCall(request).execute()


    }

}