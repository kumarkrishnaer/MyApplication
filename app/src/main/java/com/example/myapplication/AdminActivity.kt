package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AdminActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        etTitle = findViewById(R.id.tvLatestTitle)
        etMessage = findViewById(R.id.tvLatestMessage)
        btnSend = findViewById(R.id.btnSend)

        btnSend.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val message = etMessage.text.toString().trim()

            if (title.isEmpty()) {
                etTitle.error = "Enter title"
                etTitle.requestFocus()
                return@setOnClickListener
            }

            if (message.isEmpty()) {
                etMessage.error = "Enter message"
                etMessage.requestFocus()
                return@setOnClickListener
            }

            sendLatestUpdate(title, message)
        }
    }

    private fun sendLatestUpdate(title: String, message: String) {
        val data = hashMapOf(
            "title" to title,
            "message" to message,
            "time" to System.currentTimeMillis()
        )

        db.collection("latest_updates")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Notification saved", Toast.LENGTH_SHORT).show()
                etTitle.text.clear()
                etMessage.text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}