package com.akaalistudios.minesweeper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView

const val SPLASH_SCREEN = 5000

class splash_screen : AppCompatActivity() {
    lateinit var topAnim: Animation
    lateinit var textView:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        topAnim = AnimationUtils.loadAnimation(this,R.anim.animation)
        textView = findViewById(R.id.minesweeper_label)
        textView.animation = topAnim

        Handler(Looper.getMainLooper()).postDelayed({
            /* Create an Intent that will start the Menu-Activity. */
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
            finish()
        }, 1500)

    }
}