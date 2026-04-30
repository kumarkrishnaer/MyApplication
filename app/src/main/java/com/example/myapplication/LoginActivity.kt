package com.example.myapplication
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import android.widget.TextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = AppDatabase.getDatabase(this)

        // ✅ Initialize views FIRST
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val cbRemember = findViewById<CheckBox>(R.id.cbRemember)
        val btnRegister = findViewById<TextView>(R.id.btnRegister)
        val tvForgot = findViewById<TextView>(R.id.tvForgot)

        // ✅ SharedPref ONLY ONCE
        val sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE)

        // ✅ Auto-fill + Auto-login
        val isRemembered = sharedPref.getBoolean("remember", false)

        if (isRemembered) {
            val savedUser = sharedPref.getString("username", "")
            val savedPass = sharedPref.getString("password", "")

            etUsername.setText(savedUser)
            etPassword.setText(savedPass)
            cbRemember.isChecked = true

            // Optional auto-login
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // ✅ Register click
        btnRegister.setOnClickListener {
            Toast.makeText(this, "Register clicked", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // ✅ Login click
        btnLogin.setOnClickListener {

            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            val remember = cbRemember.isChecked

            CoroutineScope(Dispatchers.IO).launch {

                val user = db.userDao().login(username, password)

                runOnUiThread {
                    if (user != null) {

                        if (remember) {
                            sharedPref.edit()
                                .putBoolean("remember", true)
                                .putString("username", username)
                                .putString("password", password)
                                .apply()
                        } else {
                            sharedPref.edit().clear().apply()
                        }

                        Toast.makeText(this@LoginActivity, "Login Success", Toast.LENGTH_SHORT).show()

                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()

                    } else {
                        Toast.makeText(this@LoginActivity, "Create Account", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }



        tvForgot.setOnClickListener {
            showForgotPasswordDialog()
        }




    }
    //========================== forget password =============================
    private fun showForgotPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_forgot_password, null)

        val etUsername = dialogView.findViewById<EditText>(R.id.etForgotUsername)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<EditText>(R.id.etConfirmPassword)

        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->

                val username = etUsername.text.toString().trim()
                val newPassword = etNewPassword.text.toString().trim()
                val confirmPassword = etConfirmPassword.text.toString().trim()

                if (username.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword != confirmPassword) {
                    Toast.makeText(this, "Password not matching", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val user = db.userDao().checkUser(username)

                    if (user == null) {
                        Toast.makeText(this@LoginActivity, "Username not found", Toast.LENGTH_SHORT).show()
                    } else {
                        db.userDao().updatePassword(username, newPassword)
                        Toast.makeText(this@LoginActivity, "Password updated successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }




}