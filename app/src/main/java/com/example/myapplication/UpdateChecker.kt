package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File

object UpdateChecker {

    private const val GITHUB_API =
        "https://api.github.com/repos/kumarkrishnaer/MyApplication/releases/latest"

    fun checkForUpdate(activity: Activity, isManual: Boolean = false) {

        Thread {
            try {
                val client = OkHttpClient()

                val request = Request.Builder()
                    .url(GITHUB_API)
                    .header("Accept", "application/vnd.github+json")
                    .build()

                val response = client.newCall(request).execute()
                val responseText = response.body?.string()

                if (!response.isSuccessful || responseText.isNullOrEmpty()) {
                    activity.runOnUiThread {
                        if (isManual) {
                            AlertDialog.Builder(activity)
                                .setTitle("Update Check Failed")
                                .setMessage("GitHub response error.\nCode: ${response.code}")
                                .setPositiveButton("OK", null)
                                .show()
                        }
                    }
                    return@Thread
                }

                val obj = JSONObject(responseText)

//                val latestVersion = obj.getString("tag_name").replace("v", "")
//                val currentVersion = activity.packageManager
//                    .getPackageInfo(activity.packageName, 0)
//                    .versionName ?: "1.0"
                val latestVersion = obj.getString("tag_name")
                    .replace("v", "", ignoreCase = true)
                    .trim()

                val currentVersion = activity.packageManager
                    .getPackageInfo(activity.packageName, 0)
                    .versionName
                    ?.replace("v", "", ignoreCase = true)
                    ?.trim()
                    ?: "1.0"

                val assets = obj.getJSONArray("assets")
                var apkUrl = ""

                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.getString("name")

                    if (name.endsWith(".apk")) {
                        apkUrl = asset.getString("browser_download_url")
                        break
                    }
                }

                activity.runOnUiThread {

                    if (apkUrl.isNotEmpty() && isNewerVersion(latestVersion, currentVersion)) {
                        showUpdateDialog(activity, latestVersion, apkUrl)
                    } else {
                        if (isManual) {
                            AlertDialog.Builder(activity)
                                .setTitle("No Update Available")
                                .setMessage(
                                    "Your app is already up to date.\n\n" +
                                            "Current Version: $currentVersion\n" +
                                            "Latest Version: $latestVersion"
                                )
                                .setPositiveButton("OK", null)
                                .show()
                        }
                    }
                }

            } catch (e: Exception) {
                activity.runOnUiThread {
                    if (isManual) {
                        AlertDialog.Builder(activity)
                            .setTitle("Update Check Failed")
                            .setMessage("Reason:\n${e.message}")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            }
        }.start()
    }


    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }

        val maxLength = maxOf(latestParts.size, currentParts.size)

        for (i in 0 until maxLength) {
            val latestValue = latestParts.getOrElse(i) { 0 }
            val currentValue = currentParts.getOrElse(i) { 0 }

            if (latestValue > currentValue) return true
            if (latestValue < currentValue) return false
        }

        return false
    }

    private fun showUpdateDialog(
        context: Context,
        latestVersion: String,
        apkUrl: String
    ) {
        AlertDialog.Builder(context)
            .setTitle("New Update Available")
            .setMessage("WorkEasy App version $latestVersion is available.")
            .setPositiveButton("Update Now") { _, _ ->
                downloadApk(context, apkUrl)
            }
            .setNegativeButton("Later", null)
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun downloadApk(context: Context, apkUrl: String) {

        val fileName = "WorkEasy_Update.apk"

        val request = DownloadManager.Request(Uri.parse(apkUrl))
            .setTitle("WorkEasy Update")
            .setDescription("Downloading latest version...")
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            )

        val manager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val downloadId = manager.enqueue(request)

        Toast.makeText(context, "Downloading update...", Toast.LENGTH_LONG).show()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {

                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

                if (id == downloadId) {
                    context.unregisterReceiver(this)
                    installApk(context, fileName)
                }
            }
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun installApk(context: Context, fileName: String) {

        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )

        if (!file.exists()) {
            Toast.makeText(context, "APK not found", Toast.LENGTH_SHORT).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                intent.data = Uri.parse("package:${context.packageName}")
                context.startActivity(intent)

                Toast.makeText(
                    context,
                    "Allow install permission, then tap Update again",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        context.startActivity(intent)
    }
}