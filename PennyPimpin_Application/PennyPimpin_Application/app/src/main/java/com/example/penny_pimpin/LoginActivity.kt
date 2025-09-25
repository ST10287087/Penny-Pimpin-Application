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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginActivity: AppCompatActivity() {
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
        setContentView(R.layout.activity_login)

        val signinphrase = findViewById<TextView>(R.id.textView)
        val accphrase = findViewById<TextView>(R.id.textView1)
        val donthavephrase = findViewById<TextView>(R.id.textView2)
        val emailphrase = findViewById<EditText>(R.id.login_email_text)
        val passphrase = findViewById<EditText>(R.id.login_password_text)
        val forgotphrase = findViewById<TextView>(R.id.textView3)
        val loginbtn = findViewById<Button>(R.id.login_page_button)
        val otherphrase = findViewById<TextView>(R.id.textView4)


        db = AppDatabase.getDatabase(this)
        userDao = db.userDao()

        val email = findViewById<EditText>(R.id.login_email_text)
        val password = findViewById<EditText>(R.id.login_password_text)
        val loginBtn = findViewById<Button>(R.id.login_page_button)
        val goToRegister = findViewById<TextView>(R.id.textView2)
        val registerText = findViewById<TextView>(R.id.textView2)
        val forgotText = findViewById<TextView>(R.id.textView3)
        forgotText.text = android.text.Html.fromHtml("Forgot your <u>password?</u>")

        registerText.text = android.text.Html.fromHtml("Don't have an account? <u>Register</u>")




        loginBtn.setOnClickListener {
            val emailInput = email.text.toString().trim().lowercase()
            val passwordInput = password.text.toString().trim()

            if (emailInput.isEmpty()) {
                email.error = "Email is required"
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                email.error = "Invalid email format"
                return@setOnClickListener
            }

            if (passwordInput.isEmpty()) {
                password.error = "Password is required"
                return@setOnClickListener
            }

            GlobalScope.launch(Dispatchers.IO) {
                val found = userDao.getUserByEmail(emailInput)

                runOnUiThread {
                    if (found != null && found.password == passwordInput) {
                        val sharedPref = getSharedPreferences("userSession", MODE_PRIVATE)
                        sharedPref.edit().putInt("USER_ID", found.id).apply()

                        Toast.makeText(this@LoginActivity, "Login success!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainMenuActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid credentials!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        /*
        Attribution:
        Video:
        Login and Register – YouTube.
        Author/Channel: Easy Tuto
        Title: "Simple Login App in Android Studio | 2024"
        URL: https://youtu.be/H2potb8pGDQ?si=xTFZCwSIYGnnz3Sv
        Accessed on: 2025-04-23
         */


        goToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        when (UserSession.langu) {
            "en" -> {
                signinphrase.text = "Sign in to your"
                accphrase.text = "Account"
                donthavephrase.text = "Don't have an account? Register"
                emailphrase.hint = "Email"
                passphrase.hint = "Password"
                forgotphrase.text = "Forgot your password?"
                loginbtn.text = "Login"
                otherphrase.text = "Other sign-in options"
            }
            "af" -> {
                signinphrase.text = "Meld aan by jou"
                accphrase.text = "Rekening"
                donthavephrase.text = "Het jy nie 'n rekening nie? Registreer"
                emailphrase.hint = "E-pos"
                passphrase.hint = "Wagwoord"
                forgotphrase.text = "Het jy jou wagwoord vergeet?"
                loginbtn.text = "Teken in"
                otherphrase.text = "Ander aanmeldopsies"
            }
            "zu" -> {
                signinphrase.text = "Ngena ku-akhawunti yakho"
                accphrase.text = ""
                donthavephrase.text = "Awunayo i-akhawunti? Bhalisela"
                emailphrase.hint = "Imeyili"
                passphrase.hint = "Iphasiwedi"
                forgotphrase.text = "Ukhohlwe iphasiwedi yakho?"
                loginbtn.text = "Ngena"
                otherphrase.text = "Ezinye izindlela zokungena"
            }
        }

    }

}
