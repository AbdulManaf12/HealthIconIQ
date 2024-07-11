package com.example.healthiconiq

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val CAMERA_REQUEST_CODE = 101
    private val GALLERY_REQUEST_CODE = 102
    private var imageUri: Uri? = null
    private var selectedLanguage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermissions()

        val btnCamera = findViewById<Button>(R.id.btnCamera)
        val btnGallery = findViewById<Button>(R.id.btnGallery)
        val spinnerLanguage = findViewById<Spinner>(R.id.spinnerLanguage)
        val btnDescribe = findViewById<Button>(R.id.btnDescribe)
        val imageView = findViewById<ImageView>(R.id.imageView)

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
                   val text: String = CHATGPT_API.getData(uri, lang)

                    // Simulate fetching data
                    Toast.makeText(this, "Processing image in ${text}...", Toast.LENGTH_LONG)
                        .show()

                }
            }
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), CAMERA_REQUEST_CODE)
        }
    }

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