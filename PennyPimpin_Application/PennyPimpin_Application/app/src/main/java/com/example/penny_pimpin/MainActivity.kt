package com.example.penny_pimpin

import UserSession
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val registerButton = findViewById<Button>(R.id.register_button)
        val catchphrase = findViewById<TextView>(R.id.opening_catchphrase)
        val englishBtn = findViewById<Button>(R.id.btn_english)
        val afrikaansBtn = findViewById<Button>(R.id.btn_afrikaans)
        val isizuluBtn = findViewById<Button>(R.id.btn_isizulu)
        val selectphrase = findViewById<TextView>(R.id.language_intro)



        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)

        }
        val loginButton = findViewById<Button>(R.id.login_button)
        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        /*
        Attribution:
        Video:
        button event clicker – YouTube.
        Author/Channel: Atif Pervaiz
        Title: "Button Click Event | Android Studio | Kotlin"
        URL:https://youtu.be/QmEjLiR2rGU?si=2eMzt9CJKMWOHMDR
        Accessed on: 2025-06-04
         */

        /*
        Attribution:
        Website: How to Change the Whole App Language in Android Programmatically? - GeeksForGeeks
        Author: GeeksForGeeks
        URL:https://www.geeksforgeeks.org/how-to-change-the-whole-app-language-in-android-programmatically/
        Accessed on: 2025-06-04
         */
        englishBtn.setOnClickListener {
            UserSession.langu = "en"
            recreate() // optional: refresh UI
        }

        afrikaansBtn.setOnClickListener {
            UserSession.langu = "af"
            recreate()
        }

        isizuluBtn.setOnClickListener {
            UserSession.langu = "zu"
            recreate()
        }




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }
        if (UserSession.langu == "en") {

            catchphrase.text = "Now your finances are in one place and always under control"
            loginButton.text = "Login"
            registerButton.text = "Register"
            selectphrase.text = "Select your language preference!"

            } else if (UserSession.langu == "af")
       {
                catchphrase.text = "Nou is jou finansies op een plek en altyd onder beheer"
                loginButton.text = "Teken in"
                registerButton.text = "Registreer"
                selectphrase.text = "Kies jou taalvoorkeur!"
            }else if (UserSession.langu == "zu")
            {
                catchphrase.text = "Manje izimali zakho zisendaweni eyodwa futhi zilawulwa ngaso sonke isikhathi"
                loginButton.text = "Ngena"
                registerButton.text = "Bhalisela"
                selectphrase.text = "Khetha ulimi oluthandayo!"
            }
        }
    }





