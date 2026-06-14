package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Switch
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.viewpager2.widget.ViewPager2
import okhttp3.*
import org.json.JSONArray
import java.io.IOException
import android.os.Handler
import android.os.Looper
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private lateinit var tvTodayStatus: TextView
    private lateinit var tvLastUpdate: TextView
    private lateinit var imgProfile: ImageView
    private lateinit var recyclerLog: RecyclerView

    private lateinit var db: AppDatabase


    private val licenseUrl = "https://script.google.com/macros/s/AKfycby_mvyDwFMcowxT-hl7sZLnkygKgm139DQmsS5pEK_eHwDQbLuZAYY5X6aW98fV7wbp/exec"
    private val bannerUrl = "https://script.google.com/macros/s/AKfycby_mvyDwFMcowxT-hl7sZLnkygKgm139DQmsS5pEK_eHwDQbLuZAYY5X6aW98fV7wbp/exec?action=banners"

    private val noticeUrl = "https://script.google.com/macros/s/AKfycby_mvyDwFMcowxT-hl7sZLnkygKgm139DQmsS5pEK_eHwDQbLuZAYY5X6aW98fV7wbp/exec?action=notice"

    private val sliderHandler = Handler(Looper.getMainLooper())

    private val sliderRunnable = object : Runnable {
        override fun run() {

            val bannerViewPager =
                findViewById<ViewPager2>(R.id.bannerViewPager)

            bannerViewPager.currentItem =
                bannerViewPager.currentItem + 1

            sliderHandler.postDelayed(this, 3000)
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {

        val prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", false)

        val newMode = if (isDark)
            AppCompatDelegate.MODE_NIGHT_YES
        else
            AppCompatDelegate.MODE_NIGHT_NO

        // ✅ ONLY APPLY IF CHANGED (THIS FIXES FLICKER)
        if (AppCompatDelegate.getDefaultNightMode() != newMode) {
            AppCompatDelegate.setDefaultNightMode(newMode)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        db = AppDatabase.getDatabase(applicationContext)
        UpdateChecker.checkForUpdate(this)

        // ================= Views =================

        tvTodayStatus = findViewById(R.id.tvTodayStatus)
        tvLastUpdate = findViewById(R.id.tvLastUpdate)
        recyclerLog = findViewById(R.id.recyclerLog)

        val tvRunningText = findViewById<TextView>(R.id.tvRunningText)
        tvRunningText.isSelected = true

        loadRunningText()
        loadLogs()
        loadBanners()
        loadStatus()
//        loadUserName()
        checkAppLicense()





        // ================= Toolbar =================

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)




        //======================  notification ==========================

        val btnNotification = findViewById<ImageButton>(R.id.btnNotification)

        btnNotification.setOnClickListener {
            showNotificationDialog()
        }





        // ================= Drawer =================
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navMenu = findViewById<NavigationView>(R.id.navMenu)
        val logoutBtn = findViewById<LinearLayout>(R.id.btnLogout)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.app_name,
            R.string.app_name
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        //================   Drawer menu clicks  ===========================

        navMenu.setNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_home -> {
                    // already on home

                }

                    R.id.nav_License -> {
                        showLicenseDialog()

                    }


                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))


                }

                R.id.nav_settings -> {
                    showPasswordDialog(Dummy::class.java)
                }

                R.id.nav_Download -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(
                                Uri.parse("content://com.android.externalstorage.documents/document/primary:Download"),
                                DocumentsContract.Document.MIME_TYPE_DIR
                            )
                        }
                        startActivity(intent)
                    } catch (e: Exception) {
                        val fallback = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                        startActivity(fallback)
                    }


                }


                R.id.nav_about -> {
                    showAboutDialog()
                }

                R.id.nav_update -> {
                    Toast.makeText(this, "Checking for update...", Toast.LENGTH_SHORT).show()
                    UpdateChecker.checkForUpdate(this, true)


                }
            }

//            Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()
            drawerLayout.closeDrawers()
            true
        }


        //=======================  dark theme ===================================================

        val switchTheme = findViewById<Switch>(R.id.switchTheme)

        // prevent auto trigger
        switchTheme.setOnCheckedChangeListener(null)
        switchTheme.isChecked = isDark

        switchTheme.setOnCheckedChangeListener { _, checked ->

            val newMode = if (checked)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO

            // ✅ prevent unnecessary recreate
            if (AppCompatDelegate.getDefaultNightMode() == newMode) return@setOnCheckedChangeListener

            prefs.edit().putBoolean("dark_mode", checked).apply()

            AppCompatDelegate.setDefaultNightMode(newMode)
        }

        // ============================= Logout button click ==============================
        logoutBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->

                    val sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                    sharedPref.edit().clear().apply()

                    Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawers()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
 //========== ================= Bottom Navigation ==================================================

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> {
                    true
                }

                R.id.nav_reports -> {
                    startActivity(Intent(this, ReportAPFormActivity::class.java))
                    true
                }

                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }

                R.id.nav_Admin -> {
                    showPasswordDialog(AdminActivity::class.java)
                    true
                }

                else -> false
            }
        }

        // ============================ Main Buttons ======================================================

        val btnUpdate = findViewById<FloatingActionButton>(R.id.btnUpdate)
        btnUpdate.setOnClickListener {
            startActivity(Intent(this, UpdateActivity::class.java))
        }

        val btnbtnattendanc = findViewById<MaterialCardView>(R.id.btnattendanc)
        btnbtnattendanc.setOnClickListener {

            startActivity(Intent(this, TaskActivity::class.java))
        }

        val btngoggleform = findViewById<MaterialCardView>(R.id.btnDownTime)
        btngoggleform.setOnClickListener {
            val intent = Intent(this, GoogleFormActivity::class.java)
            intent.putExtra("url", "https://sites.google.com/view/bfih-fxbl-oss/home?authuser=0")
            startActivity(intent)

        }

        val btnhrms = findViewById<MaterialCardView>(R.id.btnhrms)

        btnhrms.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("url", "https://hrmstos.bharatfih.com/")
            startActivity(intent)
        }


        val btnShiftEndReport = findViewById<MaterialCardView>(R.id.btnShiftEndReport)

        btnShiftEndReport.setOnClickListener {
            startActivity(Intent(this, ShiftEndReportActivity::class.java))
        }

        val btnTraningDocs = findViewById<MaterialCardView>(R.id.btnTraningDocs)

        btnTraningDocs.setOnClickListener {
            startActivity(Intent(this, TraningDocs::class.java))
            intent.putExtra("url", "https://drive.google.com/drive/folders/1lI7N0Gt8gMjXyK2gcRId9NYz3uhIri9j")
        }


        //================================= HEADER ===============================



        if (navMenu.headerCount > 0) {

            val headerView = navMenu.getHeaderView(0)

            imgProfile = headerView.findViewById(R.id.imgProfile)


            val tvUserName = headerView.findViewById<TextView>(R.id.tvUserName)






            val sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE)

            val username = sharedPref.getString("username", "User")

            val formattedName = username
                ?.replaceFirstChar { it.uppercase() }

            tvUserName.text = formattedName

            // Load saved image
            val savedUri = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("profile_image_uri", null)

            if (!savedUri.isNullOrEmpty()) {
                try {
                    imgProfile.setImageURI(Uri.parse(savedUri))
                } catch (e: Exception) {
                    imgProfile.setImageResource(R.drawable.ic_profile)

                    getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        .edit()
                        .remove("profile_image_uri")
                        .apply()
                }
            } else {
                imgProfile.setImageResource(R.drawable.ic_profile)
            }

            // Click to pick image
            imgProfile.setOnClickListener {
                pickImage.launch(arrayOf("image/*"))
            }
        }


    }
// ===============================  end of oncreate View =================================

//    ==============================  logs report  ==============================

    private fun loadLogs() {

        val prefs = getSharedPreferences("ReportLogs", MODE_PRIVATE)

        val logs = prefs.getStringSet("logs", mutableSetOf())
            ?.toList()
            ?.reversed()

        recyclerLog.layoutManager =
            LinearLayoutManager(this)

        recyclerLog.adapter =
            LogAdapter(logs ?: emptyList())
    }


    private val pickImage =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null && ::imgProfile.isInitialized) {

                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    imgProfile.setImageURI(uri)

                    getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        .edit()
                        .putString("profile_image_uri", uri.toString())
                        .apply()

                } catch (e: Exception) {
                    imgProfile.setImageResource(R.drawable.ic_profile)
                }
            }
        }


    override fun onResume() {
        super.onResume()
        loadStatus()
//        loadUserName()
        loadLogs()
    }



    private fun isUpdatedToday(lastUpdate: Long): Boolean {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return lastUpdate >= todayStart
    }

    private fun formatDate(time: Long): String {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(time))
    }

    private fun loadStatus() {
        val sharedPref = getSharedPreferences("WorkPrefs", MODE_PRIVATE)
        val lastUpdateTime = sharedPref.getLong("last_update_time", 0)

        if (lastUpdateTime == 0L) {
            tvTodayStatus.text = "Not Updated"
            tvTodayStatus.setTextColor(Color.RED)
            tvLastUpdate.text = "No data"
        } else {
            if (isUpdatedToday(lastUpdateTime)) {
                tvTodayStatus.text = "Updated"
                tvTodayStatus.setTextColor(Color.parseColor("#2E7D32"))
            } else {
                tvTodayStatus.text = "Not Updated"
                tvTodayStatus.setTextColor(Color.RED)
            }

            tvLastUpdate.text = formatDate(lastUpdateTime)
        }
    }


    private fun showPasswordDialog(targetActivity: Class<*>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Admin Access")

        val input = EditText(this)
        input.hint = "Enter Password"
        input.inputType =
            android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        builder.setView(input)

        builder.setPositiveButton("Login") { _, _ ->
            val enteredPassword = input.text.toString().trim()

            if (enteredPassword.isEmpty()) {
                Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (enteredPassword == "admin123") {
                startActivity(Intent(this, targetActivity))
            } else {
                Toast.makeText(this, "Wrong Password ❌", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showAboutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_about, null)

        val tvVersion = dialogView.findViewById<TextView>(R.id.tvVersion)
        val tvDeveloper = dialogView.findViewById<TextView>(R.id.tvDeveloper)
//        val tvContact = dialogView.findViewById<TextView>(R.id.tvContact)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)

        val version = packageManager.getPackageInfo(packageName, 0).versionName
        tvVersion.text = "Version: $version"
        tvDeveloper.text = "Developed by: KRISHNA KUMAR"
//        tvContact.text = "Mail: kumarkrishna.er@gmail.com"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.show()

        btnOk.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun showNotificationDialog() {

        val notifications = getSharedPreferences("Notifications", MODE_PRIVATE)
            .getStringSet("items", emptySet())
            ?.toList()
            ?.sortedDescending()
            ?: emptyList()

        val message = if (notifications.isEmpty()) {
            "No notifications received"
        } else {
            notifications.joinToString("\n\n")
        }

        AlertDialog.Builder(this)
            .setTitle("Notifications")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    //==============================  checkAppLicense  ======================================
    private val trialMillis = 10000L // 10 seconds for testing
//    private val trialMillis = 15L * 24 * 60 * 60 * 1000

    private fun checkAppLicense() {
        val prefs = getSharedPreferences("LicensePrefs", MODE_PRIVATE)

        val installDate = prefs.getLong("installDate", 0L)
        val isActivated = prefs.getBoolean("isActivated", false)

        val today = System.currentTimeMillis()

        if (isActivated) return

        if (installDate == 0L) {
            prefs.edit().putLong("installDate", today).apply()
            return
        }

        val usedTime = today - installDate

        if (usedTime >= trialMillis) {
            showActivationDialog()
        }
    }

    private fun showActivationDialog() {
        val input = EditText(this)
        input.hint = "Enter activation code"

        AlertDialog.Builder(this)
            .setTitle("Trial Expired")
            .setMessage("Your trial period is over. Please enter activation code.")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Activate", null)
            .show()
            .apply {
                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val code = input.text.toString().trim()

                    if (code.isEmpty()) {
                        Toast.makeText(this@MainActivity, "Enter license code", Toast.LENGTH_SHORT).show()
                    } else {
                        verifyLicenseFromGoogleSheet(code)
                    }

                }
            }
    }



    private fun showLicenseDialog() {
        val prefs = getSharedPreferences("LicensePrefs", MODE_PRIVATE)

        val isActivated = prefs.getBoolean("isActivated", false)
        val installDate = prefs.getLong("installDate", 0L)
        val today = System.currentTimeMillis()

        val message = if (isActivated) {
            """
        License Status : Activated ✓
        
        License Type : Lifetime
        
        Thank you for using Work Easy.
        """.trimIndent()
        } else {
            val usedTime = today - installDate
            val remainingMillis = (trialMillis - usedTime).coerceAtLeast(0L)
//            val remainingSeconds = remainingMillis / 1000
            val days = remainingMillis / (1000 * 60 * 60 * 24)

            val hours = (remainingMillis / (1000 * 60 * 60)) % 24

            val minutes = (remainingMillis / (1000 * 60)) % 60

            """
        License Status : Trial Version
        
        Time Remaining : ${days}d ${hours}h ${minutes}m
        
        Activation Required After Trial Period
        """.trimIndent()
        }

        AlertDialog.Builder(this)
            .setTitle("Work Easy License")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }


    private fun getAndroidDeviceId(): String {
        return Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    private fun verifyLicenseFromGoogleSheet(code: String) {
        lifecycleScope.launch {
            try {
                val loginPrefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                val userName = loginPrefs.getString("username", "User") ?: "User"

                val deviceId = getAndroidDeviceId()

                val json = """
                {
                    "reportType": "license_check",
                    "code": "$code",
                    "userName": "$userName",
                    "deviceId": "$deviceId"
                }
            """.trimIndent()

                val body = json.toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(licenseUrl)
                    .post(body)
                    .build()

                val response = withContext(Dispatchers.IO) {
                    OkHttpClient().newCall(request).execute()
                }

                val result = response.body?.string() ?: ""

                val resultText = org.json.JSONObject(result).getString("result")

                when (resultText) {

                    "VALID" -> {
                        getSharedPreferences("LicensePrefs", MODE_PRIVATE)
                            .edit()
                            .putBoolean("isActivated", true)
                            .putString("licenseCode", code)
                            .putString("userName", userName)
                            .putString("deviceId", deviceId)
                            .apply()

                        Toast.makeText(this@MainActivity, "Activated successfully", Toast.LENGTH_SHORT).show()
                        recreate()
                    }

                    "ALREADY_USED" -> {
                        Toast.makeText(this@MainActivity, "Code already used on another device", Toast.LENGTH_LONG).show()
                    }

                    "INACTIVE" -> {
                        Toast.makeText(this@MainActivity, "License inactive", Toast.LENGTH_LONG).show()
                    }

                    "INVALID" -> {
                        Toast.makeText(this@MainActivity, "Invalid license code", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Internet required for activation", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //====================================== Banner post ============================================================



    private fun loadBanners() {

        val bannerViewPager = findViewById<ViewPager2>(R.id.bannerViewPager)

        val request = Request.Builder()
            .url(bannerUrl)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {

                val json = response.body?.string() ?: return
                Log.d("BANNER_RESPONSE", json)

                if (!json.trim().startsWith("[")) {
                    Log.e("BANNER_ERROR", "Invalid response: $json")
                    return
                }

                val array = JSONArray(json)
                val bannerList = mutableListOf<BannerItem>()

                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)

                    bannerList.add(
                        BannerItem(
                            title = obj.getString("title"),
                            imageUrl = obj.getString("imageUrl")
                        )
                    )
                }

                runOnUiThread {

                    val infiniteList = mutableListOf<BannerItem>()

                    repeat(100) {
                        infiniteList.addAll(bannerList)
                    }

                    bannerViewPager.adapter = BannerAdapter(infiniteList)

                    // Start from middle
                    bannerViewPager.setCurrentItem(infiniteList.size / 2, false)

                    sliderHandler.removeCallbacks(sliderRunnable)
                    sliderHandler.postDelayed(sliderRunnable, 3000)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("BANNER_ERROR", "Failed: ${e.message}")
            }
        })
    }




    override fun onDestroy() {
        sliderHandler.removeCallbacks(sliderRunnable)
        super.onDestroy()
    }

    private fun loadRunningText() {

        val tvRunningText = findViewById<TextView>(R.id.tvRunningText)

        val request = Request.Builder()
            .url(noticeUrl)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {

                val json = response.body?.string() ?: return
                val obj = JSONObject(json)
                val message = obj.getString("message")

                runOnUiThread {
                    tvRunningText.text = message
                    tvRunningText.isSelected = true
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }
        })
    }





}


