package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Button
import android.widget.EditText
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {

    private lateinit var tvTodayStatus: TextView
    private lateinit var tvLastUpdate: TextView
    private lateinit var tvGreeting: TextView
    private lateinit var tvLatestTitle: TextView
    private lateinit var tvLatestMessage: TextView

    private lateinit var imgProfile: ImageView

    private lateinit var db: AppDatabase
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ================= Views =================
        tvTodayStatus = findViewById(R.id.tvTodayStatus)
        tvLastUpdate = findViewById(R.id.tvLastUpdate)
        tvGreeting = findViewById(R.id.tvGreeting)
        tvLatestTitle = findViewById(R.id.tvLatestTitle)
        tvLatestMessage = findViewById(R.id.tvLatestMessage)

        db = AppDatabase.getDatabase(applicationContext)

        loadStatus()
        loadUserName()
        loadLatestUpdate()

        // ================= Firebase =================
        FirebaseMessaging.getInstance().subscribeToTopic("all")
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("FCM", "Subscribed to topic ALL")
                } else {
                    Log.e("FCM", "Subscription failed")
                }
            }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FCM_TOKEN", task.result ?: "null")
            }
        }

        // ================= Toolbar =================
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        // ================= Drawer =================
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        val navMenu = findViewById<NavigationView>(R.id.navMenu)
        val logoutBtn = navigationView.findViewById<LinearLayout>(R.id.btnLogout)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.app_name,
            R.string.app_name
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Drawer menu clicks
        navMenu.setNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_home -> {
                    // already on home
                }

                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true

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
            }

            Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()
            drawerLayout.closeDrawers()
            true
        }

        // Logout button click
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

        // ================= Bottom Navigation =================
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> {
                    true
                }

                R.id.nav_tasks -> {
                    startActivity(Intent(this, TaskActivity::class.java))
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

        // ================= Main Buttons =================

        val btnUpdate = findViewById<FloatingActionButton>(R.id.btnUpdate)
        btnUpdate.setOnClickListener {
            startActivity(Intent(this, UpdateActivity::class.java))
        }

        val btnbtnattendanc = findViewById<MaterialCardView>(R.id.btnattendanc)
        btnbtnattendanc.setOnClickListener {

            startActivity(Intent(this, TaskActivity::class.java))
        }

        val btngoggleform = findViewById<MaterialCardView>(R.id.btngoogleform)
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




        if (navMenu.headerCount > 0) {

            val headerView = navMenu.getHeaderView(0)

            imgProfile = headerView.findViewById(R.id.imgProfile)


            val tvUserName = headerView.findViewById<TextView>(R.id.tvUserName)

            lifecycleScope.launch {
                val lastEntry = db.workDao().getLastEntry()

                runOnUiThread {
                    if (lastEntry != null) {
                        tvUserName.text = lastEntry.name
                    } else {
                        tvUserName.text = "User"
                    }
                }
            }

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
        loadUserName()
    }

    private fun loadLatestUpdate() {
        firestore.collection("latest_updates")
            .orderBy("time", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    tvLatestTitle.text = "Latest Update"
                    tvLatestMessage.text = "Failed to load"
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val document = snapshots.documents[0]
                    val title = document.getString("title") ?: "Latest Update"
                    val message = document.getString("message") ?: "No message"

                    tvLatestTitle.text = title
                    tvLatestMessage.text = message
                } else {
                    tvLatestTitle.text = "Latest Update"
                    tvLatestMessage.text = "No updates"
                }
            }
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

    private fun loadUserName() {
        lifecycleScope.launch {
            val lastEntry = db.workDao().getLastEntry()

            runOnUiThread {
                if (lastEntry != null) {
                    setGreeting(lastEntry.name)
                } else {
                    setGreeting("User")
                }
            }
        }
    }

    private fun setGreeting(name: String) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val greeting = when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Good Night"
        }

        tvGreeting.text = "$greeting, $name"
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
        tvDeveloper.text = "Developed by: KRISHNA KUMAR (AP-TEAM)"
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
}