import android.content.Context
import android.net.Uri
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import okhttp3.logging.HttpLoggingInterceptor


object CHATGPT_API {
    private val client = OkHttpClient()

    fun getData(context: Context, imageUri: Uri, language: String, apiKey: String): String {
        val prompt = "Consider this medical symbol image and explain it or give me the meaning of it in : $language"

        context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
            val bytes = inputStream.readBytes()
            val requestBody = RequestBody.create("image/png".toMediaTypeOrNull(), bytes)

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
                    println("Error on API Call: ${e.message}")
                }
                override fun onResponse(call: Call, response: Response) {
                    response.use { resp ->
                        println("Success: ${resp.body?.string()}")
                    }
                }
            })
        } ?: run {
            println("Error: Unable to open image stream.")
            return "Failed to initiate request due to input stream error."
        }

        return "Request initiated"
    }
}
