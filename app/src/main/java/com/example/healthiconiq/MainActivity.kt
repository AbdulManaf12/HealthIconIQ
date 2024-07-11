package com.example.healthiconiq

import android.os.Bundle
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnCamera = findViewById<Button>(R.id.btnCamera)
        val btnGallery = findViewById<Button>(R.id.btnGallery)
        val spinnerLanguage = findViewById<Spinner>(R.id.spinnerLanguage)
        val imgSettings = findViewById<ImageView>(R.id.imgSettings)

        btnCamera.setOnClickListener {
            Toast.makeText(this, "Opening Camera...", Toast.LENGTH_SHORT).show()

        }

        // Set up click listener for the Gallery button
        btnGallery.setOnClickListener {
            // Implement action to open gallery
            Toast.makeText(this, "Opening Gallery...", Toast.LENGTH_SHORT).show()
            // You should start gallery picker intent here
        }

        // Set up item selected listener for the Spinner
        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedLanguage = parent?.getItemAtPosition(position).toString()
                Toast.makeText(this@MainActivity, "Selected Language: $selectedLanguage", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Optionally handle the case where nothing is selected
            }
        }

        // Set up click listener for the Settings image
        imgSettings.setOnClickListener {
            // Navigate to Settings screen
            Toast.makeText(this, "Opening Settings...", Toast.LENGTH_SHORT).show()
            // You can start a new SettingsActivity here
        }
    }
}
