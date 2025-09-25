package com.example.penny_pimpin.fragment

import UserSession
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.penny_pimpin.CategoryAdapter
import com.example.penny_pimpin.R
import com.example.penny_pimpin.data.dao.CategoryDao
import com.example.penny_pimpin.data.database.AppDatabase
import com.example.penny_pimpin.data.model.CategoryEntity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

// Fragment for managing categories
class CategoryFragment: Fragment() {
    // Initialize database and DAO for accessing category data
    private lateinit var database: AppDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var emptyViewMessage: TextView
    /*
    Attribution:
    Website: How to Change the Whole App Language in Android Programmatically? - GeeksForGeeks
    Author: GeeksForGeeks
    URL:https://www.geeksforgeeks.org/how-to-change-the-whole-app-language-in-android-programmatically/
    Accessed on: 2025-06-04
     */
    // Called when the fragment's view is created
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    // Called after the view is created, to set up RecyclerView and observe categories
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyViewMessage = view.findViewById(R.id.textEmpty)

        // Example of translating the empty message based on language:
        emptyViewMessage.text = when (UserSession.langu) {
            "en" -> "No categories yet. Add one!"
            "af" -> "Geen kategorieë nie. Voeg een by!"
            "zu" -> "Azikho izigaba okwamanje. Engeza eyodwa!"
            else -> "No categories yet. Add one!"
        }
        // Initialize database and category DAO
        database = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "app_database"
        ).build()
        categoryDao = database.categoryDao()

        // Set up RecyclerView for displaying categories in a grid layout
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerCategories)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // Get user ID from shared preferences
        val sharedPref = requireContext().getSharedPreferences("userSession", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)

        // If user is not logged in, show a toast and return
        if (userId == -1 && UserSession.langu == "en") {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        } else if (userId == -1 && UserSession.langu == "af") {
            Toast.makeText(requireContext(), "Gebruiker is nie aangemeld nie", Toast.LENGTH_SHORT).show()
            return
        } else if (userId == -1 && UserSession.langu == "zu") {
            Toast.makeText(requireContext(), "Umsebenzisi akangeni ngemvume", Toast.LENGTH_SHORT).show()
            return
        }


        // Find the empty text view to show when there are no categories
        val textEmpty = view.findViewById<TextView>(R.id.textEmpty)
        if (UserSession.langu == "en")
        {
            // Observe categories for the logged-in user
            categoryDao.getCategoriesByUserId(userId)
                .observe(viewLifecycleOwner) { categories ->
                    Log.d("CategoryDebug", "Observed categories: ${categories.size}")

                    // Show/hide the empty message based on the categories list size
                    textEmpty.visibility = if (categories.isEmpty()) View.VISIBLE else View.GONE

                    // Set up adapter for displaying categories in RecyclerView
                    val categoryAdapter = CategoryAdapter(
                        categories,
                        onCategoryClick = { selectedCategory -> openExpenseListForCategory(selectedCategory) },
                        onCategoryLongClick = { categoryToDelete -> confirmAndDeleteCategory(categoryToDelete) }
                    )
                    recyclerView.adapter = categoryAdapter
                }
        }
        else if (UserSession.langu == "af")
        {
            // Observe categories for the logged-in user
            categoryDao.getCategoriesByUserId(userId)
                .observe(viewLifecycleOwner) { categories ->
                    Log.d("CategoryDebug", "Waargenome kategorieë: ${categories.size}")

                    // Show/hide the empty message based on the categories list size
                    textEmpty.visibility = if (categories.isEmpty()) View.VISIBLE else View.GONE

                    // Set up adapter for displaying categories in RecyclerView
                    val categoryAdapter = CategoryAdapter(
                        categories,
                        onCategoryClick = { selectedCategory -> openExpenseListForCategory(selectedCategory) },
                        onCategoryLongClick = { categoryToDelete -> confirmAndDeleteCategory(categoryToDelete) }
                    )
                    recyclerView.adapter = categoryAdapter
                }
        }
        else  if (UserSession.langu == "zu")
        {
            // Observe categories for the logged-in user
            categoryDao.getCategoriesByUserId(userId)
                .observe(viewLifecycleOwner) { categories ->
                    Log.d("CategoryDebug", "Izigaba ezibukiwe: ${categories.size}")

                    // Show/hide the empty message based on the categories list size
                    textEmpty.visibility = if (categories.isEmpty()) View.VISIBLE else View.GONE

                    // Set up adapter for displaying categories in RecyclerView
                    val categoryAdapter = CategoryAdapter(
                        categories,
                        onCategoryClick = { selectedCategory -> openExpenseListForCategory(selectedCategory) },
                        onCategoryLongClick = { categoryToDelete -> confirmAndDeleteCategory(categoryToDelete) }
                    )
                    recyclerView.adapter = categoryAdapter
                }
        }


        // Set up the FAB to add a new category
        val fabAddCategory = view.findViewById<FloatingActionButton>(R.id.fabAddCategory)
        fabAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }
    }
    /*
    Attribution:
    Video:
    Dialouge box – YouTube.
    Author/Channel: Admin Grabs Media
    Title: "Alert Dialog Box in Android using Kotlin | Kotlin | Android Studio Tutorial - Quick + Easy"
    URL: https://youtu.be/V-qjrWuUFrQ?si=zEl6MmAjzapt1LFR
    Accessed on: 2025-04-28
     */
    // Opens the expense list for the selected category
    private fun openExpenseListForCategory(category: CategoryEntity) {
        val bundle = Bundle().apply {
            putInt("CATEGORY_ID", category.id)
            putString("CATEGORY_NAME", category.name)
        }

        val expenseListFragment = ExpenseListFragment()
        expenseListFragment.arguments = bundle

        // Replace current fragment with the ExpenseListFragment
        requireActivity()
            .supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, expenseListFragment)
            .addToBackStack(null)
            .commit()
    }
    /*
    Attribution:
    Video:
    Dialouge box – YouTube.
    Author/Channel: Admin Grabs Media
    Title: "Alert Dialog Box in Android using Kotlin | Kotlin | Android Studio Tutorial - Quick + Easy"
    URL: https://youtu.be/V-qjrWuUFrQ?si=zEl6MmAjzapt1LFR
    Accessed on: 2025-04-28
     */
    // Displays a dialog to add a new category
    private fun showAddCategoryDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_category, null)
        val editTextCategoryName = dialogView.findViewById<EditText>(R.id.editTextCategoryName)
        val btnSelectColor = dialogView.findViewById<Button>(R.id.btnSelectColor)

        var selectedColor = Color.GRAY  // Default color

        // Color selection dialog for the category
        btnSelectColor.setOnClickListener {
            val colors = intArrayOf(
                Color.RED, Color.GREEN, Color.BLUE,
                Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.GRAY
            )

            val colorNames = when (UserSession.langu) {
                "af" -> arrayOf("Rooi", "Groen", "Blou", "Geel", "Siaan", "Magenta", "Grys")
                "zu" -> arrayOf("Obomvu", "Oluhlaza okotshani", "Oluhlaza okwesibhakabhaka", "Ophuzi", "Sayan", "Magenta", "Mpunga")
                else -> arrayOf("Red", "Green", "Blue", "Yellow", "Cyan", "Magenta", "Gray")
            }
            val title = when (UserSession.langu) {
                "en" -> "Pick a color"
                "af" -> "Kies 'n kleur"
                "zu" -> "Khetha umbala"
                else -> "Pick a color"
            }

            AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setItems(colorNames) { _, which ->
                    selectedColor = colors[which]
                    btnSelectColor.text = colorNames[which]
                }
                .show()

        }

        // Customize the dialog title
        val btnAddCategory = dialogView.findViewById<Button>(R.id.btnAddCategory)
        val builder = AlertDialog.Builder(requireContext())
        val title = TextView(requireContext()).apply {
            if (UserSession.langu == "en")
            {
                text = "Add Category"
               editTextCategoryName.hint = "Enter Category Name"
                btnSelectColor.text = "Select Color"
              btnAddCategory.text = "Add"
            }
            else if (UserSession.langu == "af")
            {
            text = "Voeg kategorie by"
                editTextCategoryName.hint = "Voer kategorie naam in"
btnSelectColor.text = "Kies kleur"
            btnAddCategory.text = "Voeg by"
            }
            else  if (UserSession.langu == "zu")
            {
                text = "Engeza Isigaba"
                editTextCategoryName.hint = "Faka igama lesigaba"
                btnSelectColor.text = "Khetha umbala"
                btnAddCategory.text = "Engeza"
            }
            setPadding(40, 40, 40, 20)
            textSize = 20f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dialog_background))
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        builder.setCustomTitle(title)

        builder.setView(dialogView)

        // Create and show the dialog
        val dialog = builder.create()

        // Handle the positive button click for adding a category
        val positiveButton = dialogView.findViewById<Button>(R.id.btnAddCategory)
        positiveButton.setOnClickListener {
            val categoryName = editTextCategoryName.text.toString()
            val sharedPref = requireContext().getSharedPreferences("userSession", Context.MODE_PRIVATE)
            val userId = sharedPref.getInt("USER_ID", -1)

            // If user is not logged in, show a toast and dismiss the dialog
            if (userId == -1 && UserSession.langu == "en" ) {
                Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@setOnClickListener
            } else if (userId == -1 && UserSession.langu == "af") {
                Toast.makeText(requireContext(), "Gebruiker nie aangemeld nie!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@setOnClickListener
            } else if (userId == -1 && UserSession.langu == "zu") {
                Toast.makeText(requireContext(), "Umsebenzisi akangenile!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@setOnClickListener
            }

            // Add the category if the name is not blank
            if (categoryName.isNotBlank()) {
                addCategoryToDatabase(categoryName, selectedColor, userId)
                dialog.dismiss()
            } else if (UserSession.langu == "en") {
                Toast.makeText(requireContext(), "Please enter a category name", Toast.LENGTH_SHORT).show()
            }
            else if (UserSession.langu == "af") {
                Toast.makeText(requireContext(), "Voer asseblief 'n kategorienaam in", Toast.LENGTH_SHORT).show()
            }
            else if (UserSession.langu == "zu") {
                Toast.makeText(requireContext(), "Sicela ufake igama lesigaba", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.color.dialog_background)  // Set custom background
    }

    // Adds the new category to the database
    private fun addCategoryToDatabase(name: String, color: Int, userId: Int) {
        lifecycleScope.launch {
            val category = CategoryEntity(
                name = name,
                color = color,
                userId = userId
            )
            categoryDao.insertCategory(category)
        }
    }

    // Shows a confirmation dialog to delete a category
    private fun confirmAndDeleteCategory(category: CategoryEntity) {
        val title = when (UserSession.langu) {
            "en" -> "Delete Category"
            "af" -> "Vee kategorie uit"
            "zu" -> "Susa Isigaba"
            else -> "Delete Category"
        }
        val message = when (UserSession.langu) {
            "en" -> "Are you sure you want to delete '${category.name}'?"
            "af" -> "Is jy seker jy wil '${category.name}' uitvee?"
            "zu" -> "Uqinisekile ukuthi ufuna ukususa '${category.name}'?"
            else -> "Are you sure you want to delete '${category.name}'?"
        }
        val positive = when (UserSession.langu) {
            "en" -> "Delete"
            "af" -> "Vee uit"
            "zu" -> "Susa"
            else -> "Delete"
        }

        val negative = when (UserSession.langu) {
            "en" -> "Cancel"
            "af" -> "Kanselleer"
            "zu" -> "Khansela"
            else -> "Cancel"
        }

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positive) { _, _ ->
                lifecycleScope.launch {
                    categoryDao.deleteCategory(category)
                }
            }
            .setNegativeButton(negative, null)
            .show()
    }
}
