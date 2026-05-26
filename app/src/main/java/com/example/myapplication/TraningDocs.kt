package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent

class TraningDocs : AppCompatActivity() {

    private var currentUrl =
        "https://drive.google.com/drive/folders/1lI7N0Gt8gMjXyK2gcRId9NYz3uhIri9j"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentUrl =
            intent.getStringExtra("url") ?: currentUrl

        val customTabsIntent =
            CustomTabsIntent.Builder()
                .build()

        customTabsIntent.launchUrl(
            this,
            Uri.parse(currentUrl)
        )

        finish()
    }
}