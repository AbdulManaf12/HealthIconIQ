package com.example.healthiconiq

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var etApiKey: EditText
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var themeSwitch: Switch
    private lateinit var btnSaveApiKey: Button

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        etApiKey = findViewById(R.id.etApiKey)
        themeSwitch = findViewById(R.id.switchTheme)
        btnSaveApiKey = findViewById(R.id.btnSaveApiKey)


        etApiKey.setText(getApiKey())
        btnSaveApiKey.setOnClickListener {
            saveApiKey(etApiKey.text.toString())
            Toast.makeText(this, "API Key Saved", Toast.LENGTH_SHORT).show()
            finish()
        }
        applyTheme()
        themeSwitch.setOnCheckedChangeListener(null)
        val sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val isLightTheme = sharedPreferences.getString("THEME", "Light") == "Light"
        themeSwitch.isChecked = isLightTheme

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            with (sharedPreferences.edit()) {
                putString("THEME", if (isChecked) "Light" else "Dark")
                apply()
            }
            recreate()
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

    private fun applyTheme() {
        val sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val theme = sharedPreferences.getString("THEME", "Light") ?: "Light"
        val mainLayout = findViewById<LinearLayout>(R.id.main)
        if (theme == "Dark") {
            mainLayout.setBackgroundColor(Color.BLACK)
            themeSwitch.text = "Dark"
            themeSwitch.setTextColor(Color.WHITE)
            etApiKey.setTextColor(Color.WHITE)
        } else {
            mainLayout.setBackgroundColor(Color.WHITE)
            themeSwitch.text = "Light"
            themeSwitch.setTextColor(Color.BLACK)
            etApiKey.setTextColor(Color.BLACK)
        }
    }
}