package com.example.healthiconiq

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

class TTS_OpenAI_API {
    private val client = OkHttpClient()

    fun getData(text: String, apiKey: String, onResult: (ByteArray?) -> Unit) {
        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("text", text)
            .addFormDataPart("api_key", apiKey)
            .build()

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val request = Request.Builder()
            .url("https://apitts.pythonanywhere.com/tts")
            .post(multipartBody)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val mp3Bytes = response.body?.bytes()
                        onResult(mp3Bytes)
                    } else {
                        onResult(null)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }
}
