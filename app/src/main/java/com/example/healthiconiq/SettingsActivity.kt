package com.example.healthiconiq

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var etApiKey: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        etApiKey = findViewById(R.id.etApiKey)
        val btnSaveApiKey = findViewById<Button>(R.id.btnSaveApiKey)

        etApiKey.setText(getApiKey())

        btnSaveApiKey.setOnClickListener {
            saveApiKey(etApiKey.text.toString())
            Toast.makeText(this, "API Key Saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun getApiKey(): String {
        val sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        return sharedPreferences.getString("ApiKey", "") ?: ""
    }

    private fun saveApiKey(apiKey: String) {
        val sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        with (sharedPreferences.edit()) {
            putString("ApiKey", apiKey)
            apply()
        }
    }
}
