package com.example.healthiconiq

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity2 : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var imageView: ImageView
    private lateinit var tvSymbolDescription: TextView
    private lateinit var language_type : String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        imageView = findViewById(R.id.imageView)
        tvSymbolDescription = findViewById(R.id.tvSymbolDescription)
        val btnSpeak = findViewById<ImageView>(R.id.btnSpeak)
        val btnBackToHome = findViewById<Button>(R.id.btnBackToHome)

        textToSpeech = TextToSpeech(this, this)

        intent?.extras?.let {
            val uriString = it.getString("imageUri")
            val description = it.getString("description")
            this.language_type = it.getString("language_type").toString()
            tvSymbolDescription.text = description
            if (uriString != null) {
                imageView.setImageURI(Uri.parse(uriString))
            }
        }

        btnSpeak.setOnClickListener {
            speakOut(tvSymbolDescription.text.toString())
        }

        btnBackToHome.setOnClickListener {
            finish()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            when (this.language_type) {
                "English" -> textToSpeech.language = Locale.US
                "اردو Urdu" -> textToSpeech.language = Locale("ur", "PK")
                "سنڌي Sindhi" -> textToSpeech.language = Locale("sd", "PK")
                else -> textToSpeech.language = Locale.US
            }
        } else {
            Log.e("TTS", "Initialization failed")
        }
    }


    private fun speakOut(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}