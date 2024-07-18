package com.example.healthiconiq

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
    private lateinit var language_type: String
    private lateinit var btnSpeak: ImageView
    private var heartbeatAnimator: ObjectAnimator? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        imageView = findViewById(R.id.imageView)
        tvSymbolDescription = findViewById(R.id.tvSymbolDescription)
        btnSpeak = findViewById(R.id.btnSpeak)
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

    private fun startHeartbeatAnimation() {
        heartbeatAnimator = ObjectAnimator.ofPropertyValuesHolder(
            btnSpeak,
            PropertyValuesHolder.ofFloat("scaleX", 1.2f),
            PropertyValuesHolder.ofFloat("scaleY", 1.2f)
        ).apply {
            duration = 500
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
            start()
        }
    }

    private fun stopHeartbeatAnimation() {
        heartbeatAnimator?.let {
            it.end()
            btnSpeak.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
        }
    }

    private fun speakOut(text: String) {
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, "UniqueID")
        startHeartbeatAnimation()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val localeEnglish = Locale.US
            val localeUrdu = Locale("ur", "PK")
            val localeSindhi = Locale("sd", "PK")

            when (this.language_type) {
                "English" -> setLanguage(textToSpeech, localeEnglish)
                "اردو Urdu" -> setLanguage(textToSpeech, localeUrdu)
                "سنڌي Sindhi" -> setLanguage(textToSpeech, localeSindhi)
                else -> textToSpeech.language = Locale.US
            }

            textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {
                    runOnUiThread {
                        stopHeartbeatAnimation()
                    }
                }

                override fun onError(utteranceId: String?) {
                    runOnUiThread {
                        stopHeartbeatAnimation()
                    }
                }
            })

            speakOut(tvSymbolDescription.text.toString())
        } else {
            Log.e("TTS", "Initialization failed")
        }
    }

    private fun setLanguage(tts: TextToSpeech, locale: Locale) {
        if (tts.isLanguageAvailable(locale) == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
            tts.language = locale
        } else {
            Log.e("TTS", "Language ${locale.displayLanguage} not supported.")
        }
    }

    override fun onDestroy() {
        stopHeartbeatAnimation()
        if (textToSpeech != null) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}