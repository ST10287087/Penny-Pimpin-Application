package com.example.penny_pimpin.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.penny_pimpin.R
import com.example.penny_pimpin.data.database.AppDatabase
import com.example.penny_pimpin.data.model.CategoryBudgetGoalEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BudgetFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_budget, container, false)
    }
    /*
    Attribution:
    Website: How to Change the Whole App Language in Android Programmatically? - GeeksForGeeks
    Author: GeeksForGeeks
    URL:https://www.geeksforgeeks.org/how-to-change-the-whole-app-language-in-android-programmatically/
    Accessed on: 2025-06-04
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        val userId = requireContext()
            .getSharedPreferences("userSession", Context.MODE_PRIVATE)
            .getInt("USER_ID", -1)
        /*
        Attribution:
        Video:
        spinner – YouTube.
        Author/Channel: Stevdza-San
        Title: "Exposed Drop-Down Menu - Forget about Spinner | Android Studio Tutorial"
        URL: https://youtu.be/741l_fPKL3Y?si=kapFwJEhjM75RxfI
        Accessed on: 2025-05-01
         */
        val spinner = view.findViewById<Spinner>(R.id.spinnerCategory)
        val editCategoryAmount = view.findViewById<EditText>(R.id.editCategoryGoalAmount)
        val btnSaveCategory = view.findViewById<Button>(R.id.btnSaveCategoryGoal)
        val titleCatBug = view.findViewById<TextView>(R.id.titleCatBug)

        if (UserSession.langu == "en") {
            titleCatBug.text = "Per-Category Budget Limit"
            editCategoryAmount.hint = "Limit for selected category"
            btnSaveCategory.text = "Save Category Limit"
        } else if (UserSession.langu == "af") {
            titleCatBug.text = "Begrotingslimiet per Kategorie"
            editCategoryAmount.hint = "Limiet vir gekose kategorie"
            btnSaveCategory.text = "Stoor Kategorie Limiet"
        } else if (UserSession.langu == "zu") {
            titleCatBug.text = "Umkhawulo Wesabelomali Ngokwesigaba"
            editCategoryAmount.hint = "Umkhawulo wesigaba esikhethiwe"
            btnSaveCategory.text = "Londoloza Umkhawulo Wesigaba"
        }


        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val month = sdf.format(Date())

        // Load category names into spinner and attach listeners
        lifecycleScope.launch {
            val categories = db.categoryDao().getAllCategoriesForUser(userId)

            // Map category names to their IDs
            val categoryMap = categories.associateBy({ it.name }, { it.id })

            // Set up spinner adapter
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryMap.keys.toList())
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            // Handle spinner item selection
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selectedName = parent.getItemAtPosition(position).toString()
                    val categoryId = categoryMap[selectedName]

                    // Load existing goal for selected category if available
                    if (categoryId != null) {
                        lifecycleScope.launch {
                            val existingGoal = db.categoryBudgetGoalDao().getGoalForCategory(userId, categoryId, month)
                            if (existingGoal != null) {
                                editCategoryAmount.setText(existingGoal.goalAmount.toString())
                            } else {
                                editCategoryAmount.text.clear()
                            }
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            // Handle save button click
            btnSaveCategory.setOnClickListener {
                val selectedName = spinner.selectedItem?.toString()
                val categoryId = categoryMap[selectedName]
                val amount = editCategoryAmount.text.toString().toDoubleOrNull()

                val lang = UserSession.langu
                val successMsg = when (lang) {
                    "af" -> "Kategorie-doelwit gestoor!"
                    "zu" -> "Inhloso yesigaba ilondoloziwe!"
                    else -> "Category goal saved!"
                }

                val errorMsg = when (lang) {
                    "af" -> "Kies 'n kategorie en geldige bedrag"
                    "zu" -> "Khetha isigaba nenani elifanele"
                    else -> "Select a category and valid amount"
                }

                if (categoryId != null && amount != null && amount >= 0) {
                    lifecycleScope.launch {
                        db.categoryBudgetGoalDao().insertOrUpdate(
                            CategoryBudgetGoalEntity(
                                userId = userId,
                                categoryId = categoryId,
                                month = month,
                                goalAmount = amount
                            )
                        )
                        Toast.makeText(requireContext(), successMsg, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
}
