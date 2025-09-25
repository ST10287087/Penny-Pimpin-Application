package com.example.penny_pimpin.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.penny_pimpin.R
import android.app.DatePickerDialog
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penny_pimpin.data.database.AppDatabase
import com.example.penny_pimpin.data.model.Loan
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow
import kotlinx.coroutines.launch



class LoanManagementFragment:Fragment() {

    private lateinit var etStartDate: EditText
    private lateinit var etLoanAmount: EditText
    private lateinit var etInterestRate: EditText
    private lateinit var etDuration: EditText
    private lateinit var btnSubmitLoan: Button
    private lateinit var tvMonthlyRepayment: TextView

    private lateinit var db: AppDatabase
    private lateinit var rvLoans: RecyclerView
    private val calendar = Calendar.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /*
        Attribution:
        Website: How to Change the Whole App Language in Android Programmatically? - GeeksForGeeks
        Author: GeeksForGeeks
        URL:https://www.geeksforgeeks.org/how-to-change-the-whole-app-language-in-android-programmatically/
        Accessed on: 2025-06-04
         */
        val view = inflater.inflate(R.layout.fragment_loan_management, container, false)
        val tvLoanHistoryLabel = view.findViewById<TextView>(R.id.tvLoanHistoryLabel)
        etLoanAmount = view.findViewById(R.id.etLoanAmount)
        etInterestRate = view.findViewById(R.id.etInterestRate)
        etStartDate = view.findViewById(R.id.etStartDate)
        etDuration = view.findViewById(R.id.etDuration)
        btnSubmitLoan = view.findViewById(R.id.btnSubmitLoan)
        tvMonthlyRepayment = view.findViewById(R.id.tvMonthlyRepayment)
        if (UserSession.langu == "en") {
            etLoanAmount.hint = "Loan Amount"
            etInterestRate.hint = "Interest Rate (%)"
            etStartDate.hint = "Start Date (YYYY-MM-DD)"
            etDuration.hint = "Duration (months)"
            btnSubmitLoan.text = "Calculate Repayment"
            tvMonthlyRepayment.text = "Monthly Repayment: R0.00"
            tvLoanHistoryLabel.text = "Loan History"
        } else if (UserSession.langu == "af") {
            etLoanAmount.hint = "Leningsbedrag"
            etInterestRate.hint = "Rente Koers (%)"
            etStartDate.hint = "Begindatum (JJJJ-MM-DD)"
            etDuration.hint = "Duur (maande)"
            btnSubmitLoan.text = "Bereken Terugbetaling"
            tvMonthlyRepayment.text = "Maandelikse Terugbetaling: R0.00"
            tvLoanHistoryLabel.text = "Leningsgeskiedenis"
        } else if (UserSession.langu == "zu") {
            etLoanAmount.hint = "Inani Lemalimboleko"
            etInterestRate.hint = "Izinga Lenzalo (%)"
            etStartDate.hint = "Usuku Lokuqala (YYYY-MM-DD)"
            etDuration.hint = "Isikhathi (izinyanga)"
            btnSubmitLoan.text = "Bala Inkokhelo"
            tvMonthlyRepayment.text = "Inkokhelo Yanyanga Zonke: R0.00"
            tvLoanHistoryLabel.text = "Umlando Wezimali"
        }

        db = AppDatabase.getDatabase(requireContext())
        rvLoans = view.findViewById(R.id.rvLoans)
        rvLoans.layoutManager = LinearLayoutManager(requireContext())
        loadLoanList()

        setupDatePicker()

        btnSubmitLoan.setOnClickListener {
            handleLoanSubmission()
        }

        return view
    }

    private fun setupDatePicker() {// Set up date picker
        etStartDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                val formatted = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                etStartDate.setText(formatted.format(selectedDate.time))
            }, year, month, day)

            datePickerDialog.show()
        }
    }

    private fun handleLoanSubmission() { // Handle loan submission
        val amountText = etLoanAmount.text.toString()
        val rateText = etInterestRate.text.toString()
        val dateText = etStartDate.text.toString()
        val durationText = etDuration.text.toString()

        if (amountText.isBlank() || rateText.isBlank() || dateText.isBlank() || durationText.isBlank()) {
            val message = when (UserSession.langu) {
                "af" -> "Vul asseblief al die velde in."
                "zu" -> "Sicela ugcwalise wonke amasimu."
                else -> "Please fill in all fields."
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            return
        }


        val amount = amountText.toDoubleOrNull()
        val interestRate = rateText.toDoubleOrNull()
        val durationMonths = durationText.toIntOrNull()

        if (amount == null || interestRate == null || durationMonths == null || durationMonths <= 0) {
            val message = when (UserSession.langu) {
                "af" -> "Ongeldige invoerwaardes."
                "zu" -> "Okokufaka okungavumelekile."
                else -> "Invalid input values."
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            return
        }


        // Calculate monthly repayment
        val monthlyRate = interestRate / 12 / 100
        val repayment = if (monthlyRate == 0.0) {
            amount / durationMonths
        } else {
            (amount * monthlyRate) / (1 - (1 + monthlyRate).pow(-durationMonths))
        }

        val loan = Loan( // Create loan object
            amount = amount,
            interestRate = interestRate,
            startDate = dateText,
            durationMonths = durationMonths,
            monthlyRepayment = repayment
        )

        // Save to DB
        lifecycleScope.launch {
            db.loanDao().insertLoan(loan)

            val repaymentLabel = when (UserSession.langu) {
                "af" -> "Maandelikse Terugbetaling: R"
                "zu" -> "Inkokhelo yanyanga zonke: R"
                else -> "Monthly Repayment: R"
            }
            tvMonthlyRepayment.text = "$repaymentLabel${"%.2f".format(repayment)}"

            val toastMessage = when (UserSession.langu) {
                "af" -> "Lening suksesvol gestoor."
                "zu" -> "Imalimboleko igcinwe ngempumelelo."
                else -> "Loan saved successfully."
            }
            Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
        }

        loadLoanList() // refresh list
    }
    private fun loadLoanList() { // Load loan list from DB
        lifecycleScope.launch {
            val loans = db.loanDao().getAllLoans()
            rvLoans.adapter = LoanAdapter(loans)
        }
    }
}
