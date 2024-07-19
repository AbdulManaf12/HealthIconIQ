package com.example.healthiconiq

import CHATGPT_API
import DataCallback
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    private val CAMERA_PERMISSION_REQUEST_CODE = 104
    private var imageUri: Uri? = null
    private var selectedLanguage: String? = null
    private lateinit var API_KEY: String
    private val ENGLISH : String = "English"
    private val URDU : String = "اردو\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0 Urdu"
    private val SINDHI : String = "سنڌي\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0\\u00A0 Sindhi"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        var THEME: String = sharedPreferences.getString("THEME", "") ?: ""
        if (THEME.equals("")) {
            THEME = "Light"
            with(sharedPreferences.edit()) {
                putString("THEME", "Light")
                apply()
            }
        }
        if (THEME.equals("Dark")) {
            findViewById<ConstraintLayout>(R.id.main).setBackgroundColor(Color.BLACK)
        } else {
            findViewById<ConstraintLayout>(R.id.main).setBackgroundColor(Color.WHITE)
        }

        println("Theme: " + THEME)

        val btnCamera = findViewById<Button>(R.id.btnCamera)
        val btnGallery = findViewById<Button>(R.id.btnGallery)
        val spinnerLanguage = findViewById<Spinner>(R.id.spinnerLanguage)
        val btnDescribe = findViewById<Button>(R.id.btnDescribe)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val btnSettings = findViewById<ImageView>(R.id.imgSettings)

        btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
            } else {
                openCamera()
            }

        }

        btnGallery.setOnClickListener {
            openGallery()
        }

        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                selectedLanguage = parent.getItemAtPosition(position).toString()

                if (selectedLanguage.equals(ENGLISH)) {
                    selectedLanguage = "English"
                } else if (selectedLanguage.equals(URDU)) {
                    selectedLanguage = "اردو"
                } else if (selectedLanguage.equals(SINDHI)) {
                    selectedLanguage = "سنڌي"
                }

                updateDescribeButtonVisibility()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnDescribe.setOnClickListener {
            imageUri?.let { uri ->
                selectedLanguage?.let { lang ->
                    if (!isNetworkAvailable(this)) {
                        showNoConnectionDialog()
                    }

                    progressBar.visibility = View.VISIBLE
                    CoroutineScope(Dispatchers.Main).launch {

                        var text = ""
                        withContext(Dispatchers.IO) {
                            CHATGPT_API.getData(
                                this@MainActivity,
                                uri,
                                lang,
                                API_KEY,
                                object : DataCallback {
                                    override fun onSuccess(data: String) {
                                        runOnUiThread {
                                            progressBar.visibility = View.GONE
                                            val gson = Gson()
                                            val mapType =
                                                object : TypeToken<Map<String, String>>() {}.type
                                            var result: Map<String, String>?
                                            try {
                                                result = gson.fromJson(data, mapType)
                                                println(result?.get("response"))
                                                text = result?.get("response").toString()
                                            } catch (e: Exception) {
                                                showNoCorrectImageDialog()
                                            }

                                            val intent = Intent(
                                                this@MainActivity,
                                                MainActivity2::class.java
                                            ).apply {
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
                                            showNoCorrectImageDialog()
                                        }
                                    }
                                })
                        }
                    }
                }
            }
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        if (!isNetworkAvailable(this)) {
            showNoConnectionDialog()
        }

        this.API_KEY = loadApiKey()
        if (API_KEY.isEmpty()) {
            showNoAPI_KEYDialog()
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
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
               openCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to take pictures", Toast.LENGTH_SHORT).show()
            }
        }}

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
        updateImageDisplayVisibility()
    }

    private fun updateDescribeButtonVisibility() {
        val btnDescribe = findViewById<Button>(R.id.btnDescribe)
        if (imageUri != null && selectedLanguage != null) {
            btnDescribe.visibility = View.VISIBLE
        } else {
            btnDescribe.visibility = View.INVISIBLE
        }
    }

    private fun updateImageDisplayVisibility() {
        val btnDescribe = findViewById<ImageView>(R.id.imageView)
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

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities?.let {
            it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } ?: false
    }

    fun showNoConnectionDialog() {
        AlertDialog.Builder(this)
            .setTitle("No Internet Connection")
            .setMessage("Please check your internet connection and try again.")
            .setPositiveButton("Retry") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    fun showNoAPI_KEYDialog() {
        AlertDialog.Builder(this)
            .setTitle("API Error")
            .setMessage("Please check your api key and try again.")
            .setPositiveButton("Retry") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    fun showNoCorrectImageDialog() {
        AlertDialog.Builder(this)
            .setTitle("INPUT Error")
            .setMessage("Please check your image should be correct and try again.")
            .setPositiveButton("Retry") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun loadApiKey(): String {
        val sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        return sharedPreferences.getString("ApiKey", "") ?: "default_key"
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        var THEME : String = sharedPreferences.getString("THEME", "") ?: ""
        if(THEME.equals("")){
            THEME = "Light"
            with (sharedPreferences.edit()) {
                putString("THEME", "Light")
                apply()
            }
        }
        if(THEME.equals("Dark")){
            findViewById<ConstraintLayout>(R.id.main).setBackgroundColor(Color.BLACK)
        }else{
            findViewById<ConstraintLayout>(R.id.main).setBackgroundColor(Color.WHITE)
        }

        println("Theme: " + THEME)
    }
}