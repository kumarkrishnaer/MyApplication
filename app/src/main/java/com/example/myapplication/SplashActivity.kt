package com.example.myapplication

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<View>(R.id.imgSplashLogo)
        val glow = findViewById<View>(R.id.blueGlow)

        logo.alpha = 0f
        logo.scaleX = 0.25f
        logo.scaleY = 0.25f

        glow.alpha = 0f
        glow.scaleX = 0.4f
        glow.scaleY = 0.4f

        val logoIntro = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(logo, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(logo, View.SCALE_X, 0.25f, 1.18f, 1f),
                ObjectAnimator.ofFloat(logo, View.SCALE_Y, 0.25f, 1.18f, 1f)
            )
            duration = 1600
            interpolator = AccelerateDecelerateInterpolator()
        }

        val glowIntro = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(glow, View.ALPHA, 0f, 0.85f, 0.35f),
                ObjectAnimator.ofFloat(glow, View.SCALE_X, 0.4f, 2.4f),
                ObjectAnimator.ofFloat(glow, View.SCALE_Y, 0.4f, 2.4f)
            )
            duration = 1800
            interpolator = AccelerateDecelerateInterpolator()
        }

        val exitAnim = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(logo, View.ALPHA, 1f, 0f),
                ObjectAnimator.ofFloat(logo, View.SCALE_X, 1f, 1.45f),
                ObjectAnimator.ofFloat(logo, View.SCALE_Y, 1f, 1.45f),
                ObjectAnimator.ofFloat(glow, View.ALPHA, 0.35f, 0f)
            )
            duration = 650
            startDelay = 2200
            interpolator = AccelerateDecelerateInterpolator()
        }

        logoIntro.start()
        glowIntro.start()
        exitAnim.start()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 3000)
    }
}