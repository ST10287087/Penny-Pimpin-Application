package com.example.penny_pimpin

import UserSession
import android.content.Intent
import android.os.Bundle
import android.widget.Button

import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import com.example.penny_pimpin.data.dao.UserDao
import com.example.penny_pimpin.data.database.AppDatabase
import com.example.penny_pimpin.data.model.UserEntity
import com.google.android.material.button.MaterialButton

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RegisterActivity: AppCompatActivity() {
    /*
    Attribution:
    Video:
    ROOM Database Create Database Schema  – YouTube.
    Author/Channel: Stevdza-San
    Title: "ROOM Database - #1 Create Database Schema | Android Studio Tutorial "
    URL: https://youtu.be/lwAvI3WDXBY?si=WlFPOVGm0uonLEiN
    Accessed on: 2025-04-30
     */

    /*
    Attribution:
    Website: How to Change the Whole App Language in Android Programmatically? - GeeksForGeeks
    Author: GeeksForGeeks
    URL:https://www.geeksforgeeks.org/how-to-change-the-whole-app-language-in-android-programmatically/
    Accessed on: 2025-06-04
     */



    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        val createphrase = findViewById<TextView>(R.id.textViewCreate)
        val namehint = findViewById<EditText>(R.id.etUsername)
        val emailhint = findViewById<EditText>(R.id.etEmail)
        val passhint = findViewById<EditText>(R.id.etPassword)
        val regbtn = findViewById<Button>(R.id.btnRegister)
        val otherphrase = findViewById<TextView>(R.id.textviewother)

        db = AppDatabase.getDatabase(this)
        userDao = db.userDao()

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val goToLogin = findViewById<TextView>(R.id.textViewLogin)
        val underlinedText = findViewById<TextView>(R.id.textViewLogin)

        underlinedText.text = android.text.Html.fromHtml("Already have an account? <u>Login</u>")
        /*
        Attribution:
        Video:
       Login and Register – YouTube.
        Author/Channel: Easy Tuto
        Title: "Simple Login App in Android Studio | 2024"
        URL: https://youtu.be/H2potb8pGDQ?si=xTFZCwSIYGnnz3Sv
        Accessed on: 2025-04-23
         */
        btnRegister.setOnClickListener {
            val user = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim().lowercase()
            val pass = etPassword.text.toString().trim()

            val usernamePattern = "^[a-zA-Z]+$".toRegex()

            if (user.isEmpty()) {
                etUsername.error = "Username is required"
                return@setOnClickListener
            }

            if (!usernamePattern.matches(user)) {
                etUsername.error = "Username must contain only letters"
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                etEmail.error = "Email is required"
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Invalid email format"
                return@setOnClickListener
            }

            if (pass.isEmpty()) {
                etPassword.error = "Password is required"
                return@setOnClickListener
            }

            if (pass.length < 6) {
                etPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            GlobalScope.launch(Dispatchers.IO) {
                val emailExists = userDao.getUserByEmail(email)

                if (emailExists == null) {
                    val insertedId = userDao.insertUser(
                        UserEntity(email = email, password = pass, username = user)
                    )

                    runOnUiThread {
                        val sharedPref = getSharedPreferences("userSession", MODE_PRIVATE)
                        sharedPref.edit().putInt("USER_ID", insertedId.toInt()).apply()

                        Toast.makeText(this@RegisterActivity, "Registered!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Email already registered", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        goToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        when (UserSession.langu) {
            "en" -> {
                createphrase.text = "Create an account?"
                namehint.hint = "Name"
                emailhint.hint = "Email"
                passhint.hint = "Password"
                regbtn.text = "Register"
                otherphrase.text = "Other sign-up options"
            }
            "af" -> {
                createphrase.text = "Skep 'n rekening?"
                namehint.hint = "Naam"
                emailhint.hint = "E-pos"
                emailhint.hint = "E-pos"
                passhint.hint = "Wagwoord"
                regbtn.text = "Registreer"
                otherphrase.text = "Ander aanmeldopsies"
            }
            "zu" -> {
                createphrase.text = "Dala i-akhawunti?"
                namehint.hint = "Igama"
                emailhint.hint = "Imeili"
                passhint.hint = "Iphasiwedi"
                regbtn.text = "Bhalisela"
                otherphrase.text = "Ezinye izindlela zokusayina"
            }
        }

    }
}