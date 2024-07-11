package com.example.healthiconiq

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions() // Ensure permissions are checked on startup

        val btnCamera = findViewById<Button>(R.id.btnCamera)
        val btnGallery = findViewById<Button>(R.id.btnGallery)
        val spinnerLanguage = findViewById<Spinner>(R.id.spinnerLanguage)
        val imgSettings = findViewById<ImageView>(R.id.imgSettings)

        btnCamera.setOnClickListener {
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

        btnGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }

        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedLanguage = parent?.getItemAtPosition(position).toString()
                Toast.makeText(this@MainActivity, "Selected Language: $selectedLanguage", Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        imgSettings.setOnClickListener {
            Toast.makeText(this, "Opening Settings...", Toast.LENGTH_SHORT).show()
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
                displayImageWithIcon(uri)
            }
        } else if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                displayImageWithIcon(uri)
            }
        }
    }

    private fun displayImageWithIcon(imageUri: Uri) {
        val imageView = findViewById<ImageView>(R.id.imageView)
        imageView.setImageURI(imageUri)
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