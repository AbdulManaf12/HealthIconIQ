package com.example.healthiconiq

import CHATGPT_API
import DataCallback
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private val CAMERA_REQUEST_CODE = 101
    private val GALLERY_REQUEST_CODE = 102
    private val PERMISSION_REQUEST_CODE = 103
    private var imageUri: Uri? = null
    private var selectedLanguage: String? = null
    private var API_KEY = "sk-proj-s7kSoqUp3qLJWcgyAqH2T3BlbkFJJ36N5NcAXuSJz8hHGMoS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnCamera = findViewById<Button>(R.id.btnCamera)
        val btnGallery = findViewById<Button>(R.id.btnGallery)
        val spinnerLanguage = findViewById<Spinner>(R.id.spinnerLanguage)
        val btnDescribe = findViewById<Button>(R.id.btnDescribe)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        btnCamera.setOnClickListener {
            openCamera()
        }

        btnGallery.setOnClickListener {
            openGallery()
        }

        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                selectedLanguage = parent.getItemAtPosition(position).toString()
                updateDescribeButtonVisibility()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnDescribe.setOnClickListener {
            imageUri?.let { uri ->
                selectedLanguage?.let { lang ->
                    progressBar.visibility = View.VISIBLE
                    CoroutineScope(Dispatchers.Main).launch {
                        var text = ""
                        withContext(Dispatchers.IO) {
                            CHATGPT_API.getData(this@MainActivity, uri, lang, API_KEY, object : DataCallback {
                                override fun onSuccess(data: String) {
                                    runOnUiThread {
                                        progressBar.visibility = View.GONE
                                        val gson = Gson()
                                        val mapType = object : TypeToken<Map<String, String>>() {}.type
                                        val result: Map<String, String> = gson.fromJson(data, mapType)
                                        println(result["response"])
                                        text = result["response"].toString()

                                        val intent = Intent(this@MainActivity, MainActivity2::class.java).apply {
                                            putExtra("imageUri", uri.toString())
                                            putExtra("description", text)
                                            putExtra("language_type", lang)
                                        }
                                        startActivity(intent)
                                    }
                                }

                                override fun onFailure(error: String) {
                                    runOnUiThread {
                                        progressBar.visibility = View.GONE
                                        text = error
                                        val intent = Intent(this@MainActivity, MainActivity2::class.java).apply {
                                            putExtra("imageUri", uri.toString())
                                            putExtra("description", text)
                                            putExtra("language_type", lang)
                                        }
                                        startActivity(intent)
                                    }
                                }
                            })
                        }
                    }
                }
            }
        }
    }


    @Deprecated("This method has been deprecated in favor of using the Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            imageUri?.let { uri ->
                displayImage(uri)
            }
        } else if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                imageUri = uri
                displayImage(uri)
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show()
            null
        }
        photoFile?.also {
            imageUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", it)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun displayImageWithIcon(imageUri: Uri) {
        val imageView = findViewById<ImageView>(R.id.imageView)
        imageView.setImageURI(imageUri)
    }

    private fun displayImage(uri: Uri) {
        val imageView = findViewById<ImageView>(R.id.imageView)
        imageView.setImageURI(uri)
        updateDescribeButtonVisibility()
    }

    private fun updateDescribeButtonVisibility() {
        val btnDescribe = findViewById<Button>(R.id.btnDescribe)
        if (imageUri != null && selectedLanguage != null) {
            btnDescribe.visibility = View.VISIBLE
        } else {
            btnDescribe.visibility = View.INVISIBLE
        }
    }

    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return try {
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).also {
                imageUri = Uri.fromFile(it)
            }
        } catch (ex: IOException) {
            Toast.makeText(this, "Error while creating file: ${ex.localizedMessage}", Toast.LENGTH_SHORT).show()
            null
        }
    }
}