package com.example.healthiconiq

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class MainActivity2 : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var tvSymbolDescription: TextView
    private lateinit var language_type: String
    private lateinit var description: String
    private lateinit var btnSpeak: ImageView
    private lateinit var API_KEY: String
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        imageView = findViewById(R.id.imageView)
        tvSymbolDescription = findViewById(R.id.tvSymbolDescription)
        btnSpeak = findViewById(R.id.btnSpeak)
        val btnBackToHome = findViewById<Button>(R.id.btnBackToHome)

        intent?.extras?.let {
            val uriString = it.getString("imageUri")
            this.description = it.getString("description").toString()
            this.language_type = it.getString("language_type").toString()
            this.API_KEY = loadApiKey()
            tvSymbolDescription.text = description
            if (uriString != null) {
                imageView.setImageURI(Uri.parse(uriString))
            }
        }

        val api = TTS_OpenAI_API()

        btnSpeak.isEnabled = false
        startBlinkingAnimation()

        fetchAndPlayAudio(api)

        btnSpeak.setOnClickListener {
            fetchAndPlayAudio(api)
        }

        btnBackToHome.setOnClickListener {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            finish()
        }
    }

    private fun fetchAndPlayAudio(api: TTS_OpenAI_API) {
        api.getData(description, API_KEY) { mp3Bytes ->
            runOnUiThread {
                if (mp3Bytes != null) {
                    playAudio(mp3Bytes)
                } else {
                    Toast.makeText(this, "Failed to retrieve audio.", Toast.LENGTH_SHORT).show()
                    btnSpeak.isEnabled = true
                }
            }
        }
    }

    private fun playAudio(mp3Bytes: ByteArray) {
        try {
            val tempFile = File.createTempFile("audio", "mp3", cacheDir)
            val fos = FileOutputStream(tempFile)
            fos.write(mp3Bytes)
            fos.close()

            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    btnSpeak.isEnabled = true
                    stopBlinkingAnimation()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error playing audio.", Toast.LENGTH_SHORT).show()
            btnSpeak.isEnabled = true
            stopBlinkingAnimation()
        }
    }

    private fun startBlinkingAnimation() {
        btnSpeak.setBackgroundResource(R.drawable.blinking_animation)
        val animationDrawable = btnSpeak.background as AnimationDrawable
        animationDrawable.start()
    }

    private fun stopBlinkingAnimation() {
        val animationDrawable = btnSpeak.background as? AnimationDrawable
        animationDrawable?.stop()
    }

    private fun loadApiKey(): String {
        val sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        return sharedPreferences.getString("ApiKey", "") ?: "default_key"
    }
}