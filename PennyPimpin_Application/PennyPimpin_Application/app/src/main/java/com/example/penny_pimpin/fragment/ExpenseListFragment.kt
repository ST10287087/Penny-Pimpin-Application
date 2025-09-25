package com.example.penny_pimpin.fragment
import UserSession
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penny_pimpin.ExpenseActionListener
import com.example.penny_pimpin.ExpenseAdapter
import com.example.penny_pimpin.R
import com.example.penny_pimpin.data.database.AppDatabase
import com.example.penny_pimpin.data.model.ExpenseEntity
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class ExpenseListFragment : Fragment(), ExpenseActionListener {
    private var selectedImageData: ByteArray? = null
    private var selectedImagePreview: ImageView? = null
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val contentResolver = requireContext().contentResolver
            val type = contentResolver.getType(it) ?: ""
            val inputStream = contentResolver.openInputStream(it)
            val imageBytes = inputStream?.readBytes()
            val maxSizeInBytes = 2 * 1024 * 1024 // 2MB
            /*
            Attribution:
            Website: How to Change the Whole App Language in Android Programmatically? - GeeksForGeeks
            Author: GeeksForGeeks
            URL:https://www.geeksforgeeks.org/how-to-change-the-whole-app-language-in-android-programmatically/
            Accessed on: 2025-06-04
             */
            if (imageBytes == null) {
                val msg = when (UserSession.langu) {
                    "af" -> "Beeld is ongeldig."
                    "zu" -> "Isithombe asivumelekile."
                    else -> "Image is invalid."
                }
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            if (imageBytes.size > maxSizeInBytes) {
                val msg = when (UserSession.langu) {
                    "af" -> "Beeld moet minder as 2MB wees."
                    "zu" -> "Isithombe kumele sibe ngaphansi kuka-2MB."
                    else -> "Image must be less than 2MB."
                }
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }



            selectedImageData = imageBytes

            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            selectedImagePreview?.setImageBitmap(bitmap)
        }
    }
    private lateinit var recyclerExpenses: RecyclerView
    private var categoryId: Int = -1
    private lateinit var db: AppDatabase
    private lateinit var expenseAdapter: ExpenseAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_expense_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryId = arguments?.getInt("CATEGORY_ID") ?: -1
        val defaultCategoryName = when (UserSession.langu) {
            "af" -> "Onbekend"
            "zu" -> "Angaziwa"
            else -> "Unknown"
        }

        val categoryName = arguments?.getString("CATEGORY_NAME") ?: defaultCategoryName


        if (categoryId == -1) {
            val msg = when (UserSession.langu) {
                "af" -> "Ongeldige kategorie"
                "zu" -> "Isigaba esingavumelekile"
                else -> "Invalid category"
            }
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            return
        }


        val textCategoryTitle = view.findViewById<TextView>(R.id.textCategoryTitle)
        val title = when (UserSession.langu) {
            "af" -> "Uitgawes vir: $categoryName"
            "zu" -> "Izindleko zika: $categoryName"
            else -> "Expenses for: $categoryName"
        }
        textCategoryTitle.text = title


        val textEmpty = view.findViewById<TextView>(R.id.textEmpty)

        textCategoryTitle.setTextColor(Color.WHITE)
        textCategoryTitle.textSize = 20f

        db = AppDatabase.getDatabase(requireContext())
        recyclerExpenses = view.findViewById(R.id.recyclerExpenses)
        recyclerExpenses.layoutManager = LinearLayoutManager(requireContext())


        db.expenseDao().getExpensesByCategoryId(categoryId)
            .observe(viewLifecycleOwner) { expenses ->
                expenseAdapter = ExpenseAdapter(expenses, this)
                textEmpty.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE
                recyclerExpenses.adapter = expenseAdapter

                val totalAmount = expenses.sumOf { it.amount }
                val totalAmountText = when (UserSession.langu) {
                    "af" -> "Totaal: R%.2f".format(totalAmount)
                    "zu" -> "Ingqikithi: R%.2f".format(totalAmount)
                    else -> "Total: R%.2f".format(totalAmount)
                }

                view.findViewById<TextView>(R.id.textTotalAmount).text = totalAmountText
            }


        val fabAddExpense = view.findViewById<View>(R.id.fabAddExpense)
        fabAddExpense.setOnClickListener {
            val sharedPref =
                requireContext().getSharedPreferences("userSession", Context.MODE_PRIVATE)
            val userId = sharedPref.getInt("USER_ID", -1)

            if (userId != -1 && categoryId != -1) {
                showAddExpenseDialog(categoryId, userId, categoryName)
            } else {
                val errorMsg = when (UserSession.langu) {
                    "af" -> "Gebruiker nie aangemeld of kategorie ontbreek nie"
                    "zu" -> "Umsebenzisi akangeni noma isigaba siyashoda"
                    else -> "User not logged in or category missing"
                }

                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
            }
        }


        val startDateInput = view.findViewById<EditText>(R.id.editStartDate)
        val endDateInput = view.findViewById<EditText>(R.id.editEndDate)
        val filterButton = view.findViewById<Button>(R.id.btnFilterByDate)



        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        if (UserSession.langu == "en") {
            startDateInput.hint = "Start Date"
            endDateInput.hint = "End Date"
            filterButton.text = "Filter"
        } else if (UserSession.langu == "af") {
            startDateInput.hint = "Begindatum"
            endDateInput.hint = "Einddatum"
            filterButton.text = "Filtreer"
        } else if (UserSession.langu == "zu") {
            startDateInput.hint = "Usuku Lokuqala"
            endDateInput.hint = "Usuku Lokugcina"
            filterButton.text = "Hlunga"
        }


        // Set up DatePicker for the Start Date
        startDateInput.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Show DatePickerDialog when the user clicks the EditText
            DatePickerDialog(requireContext(), { _, y, m, d ->
                calendar.set(y, m, d)
                startDateInput.setText(dateFormat.format(calendar.time)) // Set the selected date to EditText
            }, year, month, day).show()
        }

        // Set up DatePicker for the End Date
        endDateInput.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Show DatePickerDialog when the user clicks the EditText
            DatePickerDialog(requireContext(), { _, y, m, d ->
                calendar.set(y, m, d)
                endDateInput.setText(dateFormat.format(calendar.time)) // Set the selected date to EditText
            }, year, month, day).show()
        }

        filterButton.setOnClickListener {
            val startDateString = startDateInput.text.toString()
            val endDateString = endDateInput.text.toString()

            if (startDateString.isNotBlank() && endDateString.isNotBlank()) {
                val startDate = dateFormat.parse(startDateString)?.time ?: 0L
                val endDate = dateFormat.parse(endDateString)?.time?.plus(24 * 60 * 60 * 1000) ?: System.currentTimeMillis()

                // Query the database with the filtered dates
                lifecycleScope.launch {
                    db.expenseDao().getExpensesByDateRange(categoryId, startDate, endDate - 1).observe(viewLifecycleOwner) { expenses ->
                        expenseAdapter.updateExpenses(expenses)  // Update the adapter with the new filtered expenses

                        val totalAmount = expenses.sumOf { it.amount }

                        // Update the TextView with the total amount
                        val totalAmountText = when (UserSession.langu) {
                            "af" -> "Totale uitgawe vir hierdie kategorie: R%.2f".format(totalAmount)
                            "zu" -> "Isamba sezindleko zale ngxenye: R%.2f".format(totalAmount)
                            else -> "Total Expense For this Category: R%.2f".format(totalAmount)
                        }

                        view.findViewById<TextView>(R.id.textTotalAmount).text = totalAmountText
                    }
                }
            } else {
                val message = when (UserSession.langu) {
                    "af" -> "Kies asseblief beide begin- en einddatums"
                    "zu" -> "Sicela ukhethe kokubili izinsuku zokuqala nezokugcina"
                    else -> "Please select both start and end dates"
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun filterExpensesByDate(startDate: String, endDate: String) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        try {
            val start = dateFormat.parse(startDate)
            val end = dateFormat.parse(endDate)

            if (start != null && end != null) {
                val filteredExpenses = db.expenseDao().getExpensesByCategoryId(categoryId)
                    .value?.filter {
                        val expenseDate = dateFormat.parse(it.startDateTime)
                        expenseDate != null && expenseDate.after(start) && expenseDate.before(end)
                    }

                // Update the adapter with filtered data
                expenseAdapter.updateExpenses(filteredExpenses ?: listOf())
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }
    private fun showAddExpenseDialog(categoryId: Int, userId: Int, categoryName: String) {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_expense, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.editExpenseName)
        val amountInput = dialogView.findViewById<EditText>(R.id.editExpenseAmount)
        val dateInput = dialogView.findViewById<EditText>(R.id.editExpenseDate)
        val descInput = dialogView.findViewById<EditText>(R.id.editExpenseDescription)


        dateInput.isFocusable = false
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        dateInput.setOnClickListener {
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            val day = cal.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, y, m, d ->
                cal.set(y, m, d)
                dateInput.setText(dateFormat.format(cal.time))
            }, year, month, day).show()
        }

        val btnSelectImage = dialogView.findViewById<Button>(R.id.btnSelectImage)
        selectedImagePreview = dialogView.findViewById(R.id.imageReceipt)
        btnSelectImage.setOnClickListener {
            pickImage.launch("image/*")

        }
        val editExpenseName = dialogView.findViewById<EditText>(R.id.editExpenseName)
        val editExpenseAmount = dialogView.findViewById<EditText>(R.id.editExpenseAmount)
        val editExpenseDate = dialogView.findViewById<EditText>(R.id.editExpenseDate)
        val  editExpenseDescription = dialogView.findViewById<EditText>(R.id.editExpenseDescription)
        val customTitle = TextView(requireContext()).apply {
            if (UserSession.langu == "en") {
                text = "Add Expense"
                editExpenseName.hint = "Expense Name"
                editExpenseAmount.hint = "Amount"
                editExpenseDate.hint = "Date (DD-MM-YYYY)"
                editExpenseDescription.hint = "Description"
                btnSelectImage.text = "Select Receipt"
            } else if (UserSession.langu == "af") {
                text = "Voeg Uitgawe By"
                editExpenseName.hint = "Uitgawe Naam"
                editExpenseAmount.hint = "Bedrag"
                editExpenseDate.hint = "Datum (DD-MM-JJJJ)"
                editExpenseDescription.hint = "Beskrywing"
                btnSelectImage.text = "Kies Kwitansie"
            } else if (UserSession.langu == "zu") {
                text = "Engeza Izindleko"
                editExpenseName.hint = "Igama Lezindleko"
                editExpenseAmount.hint = "Inani"
                editExpenseDate.hint = "Usuku (DD-MM-YYYY)"
                editExpenseDescription.hint = "Incazelo"
                btnSelectImage.text = "Khetha Irekhodi"
            }


            textSize = 20f
            setTextColor(Color.BLACK) // Set title text color
            gravity = Gravity.CENTER // Centralize the title
        }
        val positive = when (UserSession.langu) {
            "en" -> "Save"
            "af" -> "Stoor"
            "zu" -> "Londoloza"
            else -> "Save"
        }
        AlertDialog.Builder(requireContext())
            .setCustomTitle(customTitle)
            .setView(dialogView)
            .setPositiveButton(positive) { _, _ ->
                val name = nameInput.text.toString()
                val amount = amountInput.text.toString().toDoubleOrNull()
                val selectedDate = dateInput.text.toString()
                val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val date = inputFormat.parse(selectedDate)
                val timestampString = date?.time?.toString()


                val desc = descInput.text.toString()

                if (name.isNotBlank() && amount != null && selectedDate.isNotBlank() && desc.isNotBlank()) {
                    val expense = ExpenseEntity(
                        name = name,
                        amount = amount,
                        startDateTime = timestampString ?: "",
                        imageData = selectedImageData,
                        description = desc,
                        dateCreated = System.currentTimeMillis().toString(),
                        categoryId = categoryId,
                        userId = userId,
                        categoryName = categoryName
                    )

                    lifecycleScope.launch {
                        db.expenseDao().insertExpense(expense)


                        val addedMessage = when (UserSession.langu) {
                            "af" -> "Uitgawe bygevoeg"
                            "zu" -> "Izindleko zingeziwe"
                            else -> "Expense added"
                        }

                        Toast.makeText(requireContext(), addedMessage, Toast.LENGTH_SHORT).show()
                    }

                }
                val fillFieldsMessage = when (UserSession.langu) {
                "af" -> "Vul asseblief al die vereiste velde in"
                "zu" -> "Sicela ugcwalise wonke amasimu adingekayo"
                else -> "Please fill all required fields"
            }

                Toast.makeText(requireContext(), fillFieldsMessage, Toast.LENGTH_SHORT).show()

            }
            .setNegativeButton(
                when (UserSession.langu) {
                    "af" -> "Kanselleer"
                    "zu" -> "Khansela"
                    else -> "Cancel"
                },
                null
            )

            .show()
    }
    override fun onEditExpense(expense: ExpenseEntity) {
        showEditExpenseDialog(expense)
    }

    override fun onDeleteExpense(expense: ExpenseEntity) {
        val title = when (UserSession.langu) {
            "en" -> "Delete Expense"
            "af" -> "Verwyder Uitgawe"
            "zu" -> "Susa Izindleko"
            else -> "Delete Expense"
        }
        val deleteMessage = when (UserSession.langu) {
            "af" -> "Is jy seker jy wil \"${expense.name}\" verwyder?"
            "zu" -> "Uqinisekile ukuthi ufuna ukususa u-\"${expense.name}\"?"
            else -> "Are you sure you want to delete \"${expense.name}\"?"
        }
        val positive = when (UserSession.langu) {
            "en" -> "Delete"
            "af" -> "Vee uit"
            "zu" -> "Susa"
            else -> "Delete"
        }

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(deleteMessage)
            .setPositiveButton(positive) { _, _ ->
                lifecycleScope.launch {
                    db.expenseDao().deleteExpense(expense)
                }
            }
            .setNegativeButton(
                when (UserSession.langu) {
                    "af" -> "Kanselleer"
                    "zu" -> "Khansela"
                    else -> "Cancel"
                },
                null
            )
            .show()
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
    private fun showEditExpenseDialog(expense: ExpenseEntity) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_expense, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.editExpenseName)
        val amountInput = dialogView.findViewById<EditText>(R.id.editExpenseAmount)
        val dateInput = dialogView.findViewById<EditText>(R.id.editExpenseDate)
        val descInput = dialogView.findViewById<EditText>(R.id.editExpenseDescription)
        val imagePreview = dialogView.findViewById<ImageView>(R.id.imageReceipt)
        selectedImagePreview = imagePreview
        expense.imageData?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            imagePreview.setImageBitmap(bitmap)
            selectedImageData = it
        }
        val btnSelectImage = dialogView.findViewById<Button>(R.id.btnSelectImage)
        btnSelectImage.setOnClickListener {
            pickImage.launch("image/*")
        }
        nameInput.setText(expense.name)
        amountInput.setText(expense.amount.toString())
        dateInput.setText(expense.startDateTime)
        descInput.setText(expense.description)

        val cal = Calendar.getInstance()
        dateInput.setOnClickListener {
            val dateParts = expense.startDateTime.split("-")
            val year = dateParts[2].toInt()
            val month = dateParts[1].toInt() - 1
            val day = dateParts[0].toInt()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                val formatted = String.format("%02d-%02d-%04d", d, m + 1, y)
                dateInput.setText(formatted)
            }, year, month, day).show()
        }
        val editTitle = when (UserSession.langu) {
            "af" -> "Wysig Uitgawe"
            "zu" -> "Hlela Izindleko"
            else -> "Edit Expense"
        }

        val updateText = when (UserSession.langu) {
            "af" -> "Werk By"
            "zu" -> "Buyekeza"
            else -> "Update"
        }
        if (UserSession.langu == "en")
        {
            btnSelectImage.text = "Select Receipt"
        }
        else if (UserSession.langu == "af")
        {
            btnSelectImage.text = "Kies Kwitansie"
        }
        else if (UserSession.langu == "zu")
        {
            btnSelectImage.text = "Khetha Irekhodi"
        }
        AlertDialog.Builder(requireContext())
            .setTitle(editTitle)
            .setView(dialogView)
            .setPositiveButton(updateText) { _, _ ->
                val name = nameInput.text.toString().trim()
                val amountText = amountInput.text.toString().trim()
                val date = dateInput.text.toString().trim()
                val desc = descInput.text.toString().trim()



                if (name.isEmpty()) {
                    val nameError = when (UserSession.langu) {
                        "af" -> "Naam is verpligtend"
                        "zu" -> "Igama liyadingeka"
                        else -> "Name is required"
                    }

                    val nameToast = when (UserSession.langu) {
                        "af" -> "Naam van uitgawe is verpligtend"
                        "zu" -> "Igama lezindleko liyadingeka"
                        else -> "Name of expense is required"
                    }

                    nameInput.error = nameError
                    Toast.makeText(requireContext(), nameToast, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }


                val amount = amountText.toDoubleOrNull()
                if (amountText.isEmpty() || amount == null || amount <= 0) {
                    val amountError = when (UserSession.langu) {
                        "af" -> "Voer 'n geldige bedrag in"
                        "zu" -> "Faka inani elifanele"
                        else -> "Enter a valid amount"
                    }

                    val amountToast = when (UserSession.langu) {
                        "af" -> "Voer 'n geldige uitgawebedrag in"
                        "zu" -> "Faka inani lezindleko elifanele"
                        else -> "Enter a valid expense amount"
                    }

                    amountInput.error = amountError
                    Toast.makeText(requireContext(), amountToast, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }


                if (date.isEmpty()) {
                    val dateError = when (UserSession.langu) {
                        "af" -> "Datum is vereis"
                        "zu" -> "Usuku luyadingeka"
                        else -> "Date is required"
                    }

                    dateInput.error = dateError
                    Toast.makeText(requireContext(), dateError, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }


                if (desc.isEmpty()) {
                    val descError = when (UserSession.langu) {
                        "af" -> "Beskrywing is vereis"
                        "zu" -> "Incazelo iyadingeka"
                        else -> "Description is required"
                    }

                    descInput.error = descError
                    Toast.makeText(requireContext(), descError, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }


                val updatedExpense = expense.copy(
                    name = name,
                    amount = amount,
                    startDateTime = date,
                    description = desc,
                    imageData = selectedImageData
                )

                lifecycleScope.launch {
                    val updatedMessage = when (UserSession.langu) {
                        "af" -> "Uitgawe opgedateer"
                        "zu" -> "Izindleko zibuyekeziwe"
                        else -> "Expense updated"
                    }

                    Toast.makeText(requireContext(), updatedMessage, Toast.LENGTH_SHORT).show()

                    db.expenseDao().updateExpense(updatedExpense)
                    Toast.makeText(requireContext(), updatedMessage, Toast.LENGTH_SHORT).show()
                }
            }

            .setNegativeButton(
                when (UserSession.langu) {
                    "af" -> "Kanselleer"
                    "zu" -> "Khansela"
                    else -> "Cancel"
                },
                null
            )
            .show()
    }
}