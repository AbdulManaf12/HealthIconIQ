import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import okhttp3.logging.HttpLoggingInterceptor

interface DataCallback {
    fun onSuccess(data: String)
    fun onFailure(error: String)
}

object CHATGPT_API {
    private val client = OkHttpClient()

    fun getData(context: Context, imageUri: Uri, language: String, apiKey: String, callback: DataCallback) {
        val prompt = "Consider this medical symbol image and explain it or give me the meaning of it with in short limit of one sentence in: $language"
        Log.d("API key in getData: ",apiKey)
        context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
            val bytes = inputStream.readBytes()
            val requestBody = bytes.toRequestBody("image/png".toMediaTypeOrNull(), 0)

            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("prompt", prompt)
                .addFormDataPart("language", language)
                .addFormDataPart("api_key", apiKey)
                .addFormDataPart("image", "image.png", requestBody)
                .build()

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            val request = Request.Builder()
                .url("https://medical.pythonanywhere.com/process_image")
                .post(multipartBody)
                .build()

            println("Request details: ${request.headers}")

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onFailure("Error on API Call: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use { resp ->
                        val responseBody = resp.body?.string()
                        if (responseBody != null) {
                            callback.onSuccess(responseBody)
                        } else {
                            callback.onFailure("Received empty response")
                        }
                    }
                }
            })
        } ?: run {
            println("Error: Unable to open image stream.")
            callback.onFailure("Failed to initiate request due to input stream error.")
        }
    }
}
