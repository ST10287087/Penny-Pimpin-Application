package com.example.penny_pimpin


import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.penny_pimpin.fragment.AnalyticsFragment
import com.example.penny_pimpin.fragment.BudgetFragment
import com.example.penny_pimpin.fragment.CategoryFragment
import com.example.penny_pimpin.fragment.HomeFragment
import com.example.penny_pimpin.fragment.LoanManagementFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainMenuActivity: AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        /*
        Attribution:
        Video:
        Nav bar fragments – YouTube.
        Author/Channel: Android Mastermind
        Title: "How to create custom bottom navigation bar in android studio || Kotlin || bottom navigation bar"
        URL: https://youtu.be/bKLDhYKrMZo?si=fXKKNByLSoG2IK8l
        Accessed on: 2025-04-26
         */

        /*
        Attribution:
        Website: How to Change the Whole App Language in Android Programmatically? - GeeksForGeeks
        Author: GeeksForGeeks
        URL:https://www.geeksforgeeks.org/how-to-change-the-whole-app-language-in-android-programmatically/
        Accessed on: 2025-06-04
         */


        bottomNavigationView = findViewById(R.id.bottomNavigation)

        // Change nav bar text based on language
        when (UserSession.langu) {
            "en" -> {
                bottomNavigationView.menu.findItem(R.id.nav_budget).title = "Budget Goals"
                bottomNavigationView.menu.findItem(R.id.nav_analytics).title = "Analytics"
                bottomNavigationView.menu.findItem(R.id.nav_home).title = "Home"
                bottomNavigationView.menu.findItem(R.id.nav_categories).title = "Categories"
             bottomNavigationView.menu.findItem(R.id.nav_loan_management).title = "Loan Management"
            }
            "af" -> {
                bottomNavigationView.menu.findItem(R.id.nav_budget).title = "Begroting Doelwitte"
                bottomNavigationView.menu.findItem(R.id.nav_analytics).title = "Ontleding"
                bottomNavigationView.menu.findItem(R.id.nav_home).title = "Tuis"
                bottomNavigationView.menu.findItem(R.id.nav_categories).title = "Kategorieë"
              bottomNavigationView.menu.findItem(R.id.nav_loan_management).title = "Leningsbestuur"
            }
            "zu" -> {
                bottomNavigationView.menu.findItem(R.id.nav_budget).title = "Izinhloso Zesabelomali"
                bottomNavigationView.menu.findItem(R.id.nav_analytics).title = "Ukuhlaziywa"
                bottomNavigationView.menu.findItem(R.id.nav_home).title = "Ikhaya"
                bottomNavigationView.menu.findItem(R.id.nav_categories).title = "Izigaba"
               bottomNavigationView.menu.findItem(R.id.nav_loan_management).title = "Ukuphathwa Kwemalimboleko"
            }
        }

        bottomNavigationView.selectedItemId = R.id.nav_home
        loadFragment(HomeFragment()) // Default fragment

        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_analytics -> loadFragment(AnalyticsFragment())
                R.id.nav_budget -> loadFragment(BudgetFragment())
                R.id.nav_categories -> loadFragment(CategoryFragment())
                R.id.nav_loan_management -> loadFragment(LoanManagementFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.slide_in_right,  // enter
            R.anim.slide_out_left,  // exit
            R.anim.slide_in_left,   // popEnter
            R.anim.slide_out_right  // popExit
        )
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

}



