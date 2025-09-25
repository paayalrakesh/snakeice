package com.fake.snakeice.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fake.snakeice.databinding.ActivityAuthBinding
import com.fake.snakeice.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    // Plain SDK (no KTX helpers, no delegates)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Already logged in? Go straight to game
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()
            if (email.isEmpty() || pass.isEmpty()) {
                toast("Enter email & password"); return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    toast(task.exception?.localizedMessage ?: "Login failed")
                }
            }
        }

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()
            if (username.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                toast("Enter username, email & password"); return@setOnClickListener
            }
            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    toast(task.exception?.localizedMessage ?: "Register failed")
                    return@addOnCompleteListener
                }
                val user = auth.currentUser ?: return@addOnCompleteListener
                user.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()
                ).addOnCompleteListener {
                    db.collection("users").document(user.uid).set(
                        mapOf(
                            "username" to username,
                            "email" to email,
                            "createdAt" to System.currentTimeMillis()
                        )
                    )
                }
                toast("Registered as $username")
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        binding.btnContinueGuest.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
