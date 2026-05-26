package com.example.myapplication

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvSub = findViewById<TextView>(R.id.tvTyping)
        val orangeLine = findViewById<View>(R.id.orangeLine)
        val blueGlow = findViewById<View>(R.id.blueGlow)

        tvTitle.alpha = 0f
        tvTitle.scaleX = 0.35f
        tvTitle.scaleY = 0.35f

        tvSub.alpha = 0f
        tvSub.translationY = 35f

        orangeLine.scaleX = 0f
        orangeLine.pivotX = 0f

        blueGlow.alpha = 0f
        blueGlow.scaleX = 0.2f
        blueGlow.scaleY = 0.2f

        val glowAnim = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(blueGlow, "alpha", 0f, 0.20f, 0f),
                ObjectAnimator.ofFloat(blueGlow, "scaleX", 0.2f, 4.5f),
                ObjectAnimator.ofFloat(blueGlow, "scaleY", 0.2f, 4.5f)
            )
            duration = 1200
            interpolator = AccelerateDecelerateInterpolator()
        }

        val titleZoom = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(tvTitle, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(tvTitle, "scaleX", 0.35f, 1.15f, 1f),
                ObjectAnimator.ofFloat(tvTitle, "scaleY", 0.35f, 1.15f, 1f)
            )
            duration = 1300
            startDelay = 250
            interpolator = AccelerateDecelerateInterpolator()
        }

        val lineSweep = ObjectAnimator.ofFloat(orangeLine, "scaleX", 0f, 1f).apply {
            duration = 700
            startDelay = 1100
            interpolator = DecelerateInterpolator()
        }

        val subFade = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(tvSub, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(tvSub, "translationY", 35f, 0f)
            )
            duration = 700
            startDelay = 1500
            interpolator = DecelerateInterpolator()
        }

        val exitZoom = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(tvTitle, "scaleX", 1f, 1.6f),
                ObjectAnimator.ofFloat(tvTitle, "scaleY", 1f, 1.6f),
                ObjectAnimator.ofFloat(tvTitle, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(tvSub, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(orangeLine, "alpha", 1f, 0f)
            )
            duration = 700
            startDelay = 2700
            interpolator = AccelerateDecelerateInterpolator()
        }

        glowAnim.start()
        titleZoom.start()
        lineSweep.start()
        subFade.start()
        exitZoom.start()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 3600)
    }
}