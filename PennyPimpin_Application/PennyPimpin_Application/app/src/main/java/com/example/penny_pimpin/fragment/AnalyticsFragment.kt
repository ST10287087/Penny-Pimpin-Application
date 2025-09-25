package com.example.penny_pimpin.fragment

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.penny_pimpin.R
import com.example.penny_pimpin.data.database.AppDatabase
import com.example.penny_pimpin.data.model.CategoryBudgetGoalEntity
import com.example.penny_pimpin.data.model.CategoryTotal
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class AnalyticsFragment:Fragment() {

    private lateinit var barChart: BarChart
    private lateinit var btnSelectDateRange: Button


    private var startDate: Long = getStartOfCurrentMonth()
    private var endDate: Long = getEndOfCurrentMonth()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_analytics, container, false)

        // Setup barChart and date button
        barChart = view.findViewById(R.id.barChart)
        btnSelectDateRange = view.findViewById(R.id.btnSelectDateRange)

        // Add this block below
        val bugtitle = view.findViewById<TextView>(R.id.bugtitle)
        val btnSelectDateRangeLabel = view.findViewById<Button>(R.id.btnSelectDateRange)
        /*
        Attribution:
        Website: How to Change the Whole App Language in Android Programmatically? - GeeksForGeeks
        Author: GeeksForGeeks
        URL:https://www.geeksforgeeks.org/how-to-change-the-whole-app-language-in-android-programmatically/
        Accessed on: 2025-06-04
         */
        if (UserSession.langu == "en") {
            bugtitle.text = "Budget Analytics Overview"
            btnSelectDateRangeLabel.text = "Select Date Range"
        } else if (UserSession.langu == "af") {
            bugtitle.text = "Begroting Analitiese Oorsig"
            btnSelectDateRangeLabel.text = "Kies Datumreeks"
        } else if (UserSession.langu == "zu") {
            bugtitle.text = "Uhlaziyo Lwesabelomali"
            btnSelectDateRangeLabel.text = "Khetha Ububanzi Benyanga"
        }

        // Date picker click listener
        btnSelectDateRange.setOnClickListener {
            showDateRangePicker()
        }

        // Load chart
        loadChartData()

        return view
    }


    private fun showDateRangePicker() {
        val cal = Calendar.getInstance()

        val startDatePicker = DatePickerDialog(requireContext(), { _, y, m, d ->
            startDate = getStartOfDayTimestamp(y, m, d)
            val title = when (UserSession.langu) {
                "en" -> "Select End Date"
                "af" -> "Kies Einddatum"
                "zu" -> "Khetha Usuku Lokuphela"
                else -> "Select End Date"
            }




            val endDatePicker = DatePickerDialog(requireContext(), { _, y2, m2, d2 ->
                endDate = getEndOfDayTimestamp(y2, m2, d2)
                loadChartData()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))

            endDatePicker.setTitle(title) // Add title
            endDatePicker.show()

        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        val head = when (UserSession.langu) {
            "en" -> "Select Start Date"
            "af" -> "Kies Begindatum"
            "zu" -> "Khetha Usuku Lokuqala"
            else -> "Select Start Date"
        }
        startDatePicker.setTitle(head) // Add title
        startDatePicker.show()
    }

   /* private fun showDateRangePicker() {
        val cal = Calendar.getInstance()

        DatePickerDialog(requireContext(), { _, y, m, d ->
            startDate = getStartOfDayTimestamp(y, m, d)

            DatePickerDialog(requireContext(), { _, y2, m2, d2 ->
                endDate = getEndOfDayTimestamp(y2, m2, d2)
                loadChartData()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()

        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }*/




    private fun getStartOfDayTimestamp(year: Int, month: Int, day: Int): Long {
        return Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfDayTimestamp(year: Int, month: Int, day: Int): Long {
        return Calendar.getInstance().apply {
            set(year, month, day, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    /*
    Attribution:
    Video:
    Bar Chart – YouTube.
    Author/Channel: Admin Grabs Media
    Title: "Android Display Bar Graph using Kotlin - Android Studio 2023 - Part 1"
    URL: https://youtu.be/-TGUV_LbcmE?si=755gvWFH2XY9BYs9
    Accessed on: 2025-06-07
     */


    private fun loadChartData() {


        lifecycleScope.launch {
            try {
                val userId = getCurrentUserId()
                val db = AppDatabase.getDatabase(requireContext())

                // Get all expenses and filter manually by comparing timestamp ranges
                val allExpenses = db.expenseDao().getAllExpensesForUser(userId)

                val filteredExpenses = allExpenses.filter { expense ->
                    val timestamp = expense.startDateTime.toLongOrNull()
                    timestamp != null && timestamp in startDate..endDate
                }

                // Group by categoryId and sum amounts
                val totalsMap = filteredExpenses.groupBy { it.categoryId }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }

                // Convert to CategoryTotal with category names
                val resolvedTotals = totalsMap.mapNotNull { (categoryId, total) ->
                    val categoryName = db.categoryDao().getCategoryName(categoryId)
                    categoryName?.let { CategoryTotal(it, total) }
                }

                // Get budget goals as before
                val startMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(startDate))
                val endMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(endDate))
                val budgetGoals = db.categoryBudgetGoalDao().getGoalsForUserAndPeriod(userId, startMonth, endMonth)

                withContext(Dispatchers.Main) {
                    displayChart(resolvedTotals, budgetGoals)
                }

            } catch (e: Exception) {
                Log.e("AnalyticsFragment", "Chart Load Error: ${e.message}", e)
            }
        }
    }


    private suspend fun displayChart(
        expenseTotals: List<CategoryTotal>,
        budgetGoals: List<CategoryBudgetGoalEntity>
    ) {
        val categories = mutableSetOf<String>()  // use set to avoid duplicates
        val spentMap = expenseTotals.associateBy({ it.categoryName }, { it.total })

        val db = AppDatabase.getDatabase(requireContext())

        // Map of category name -> goal amount
        val goalNameMap = mutableMapOf<String, Double>()
        for (goal in budgetGoals) {
            val name = db.categoryDao().getCategoryName(goal.categoryId)
            if (name != null) {
                goalNameMap[name] = goal.goalAmount
                categories.add(name) // include categories with goals
            }
        }

        categories.addAll(spentMap.keys) // include categories with expenses

        val categoryList = categories.toList()
        val spentEntries = mutableListOf<BarEntry>()
        val goalEntries = mutableListOf<BarEntry>()

        categoryList.forEachIndexed { index, category ->
            val spent = spentMap[category]?.toFloat() ?: 0f
            val goal = goalNameMap[category]?.toFloat() ?: 0f

            spentEntries.add(BarEntry(index.toFloat(), spent))
            goalEntries.add(BarEntry(index.toFloat(), goal))
        }

        val spentLabel = when (UserSession.langu) {
            "en" -> "Spent"
            "af" -> "Bestee"
            "zu" -> "Okusetshenzisiwe"
            else -> "Spent"
        }

        val spentSet = BarDataSet(spentEntries, spentLabel).apply {
            color = Color.RED
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "R%.2f".format(value)
                }
            }
        }


        val goalLabel = when (UserSession.langu) {
            "en" -> "Goal"
            "af" -> "Teiken"
            "zu" -> "Inhloso"
            else -> "Goal"
        }

        val goalSet = BarDataSet(goalEntries, goalLabel).apply {
            color = Color.GREEN
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "R%.2f".format(value)
                }
            }
        }

        // val spentSet = BarDataSet(spentEntries, "Spent").apply { color = Color.RED }

        //val goalSet = BarDataSet(goalEntries, "Goal").apply { color = Color.GREEN }

        val barData = BarData(spentSet, goalSet)
        val groupSpace = 0.2f
        val barSpace = 0.05f
        val barWidth = 0.3f
        barData.barWidth = barWidth

        barChart.data = barData
        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(categoryList)
            granularity = 1f
            setCenterAxisLabels(true)
            isGranularityEnabled = true
            position = XAxis.XAxisPosition.BOTTOM
        }

        barChart.axisLeft.apply {
            axisMinimum = 0f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "R%.2f".format(value)
                }
            }
        }
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.isEnabled = false

        barChart.xAxis.axisMinimum = 0f
        barChart.xAxis.axisMaximum = 0f + barChart.barData.getGroupWidth(groupSpace, barSpace) * categoryList.size
        barChart.groupBars(0f, groupSpace, barSpace)
        barChart.description.isEnabled = false
        barChart.invalidate()
    }


    private fun getStartOfCurrentMonth(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun getEndOfCurrentMonth(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    private fun getCurrentUserId(): Int {
        val sharedPref = requireContext().getSharedPreferences("userSession", Context.MODE_PRIVATE)
        return sharedPref.getInt("USER_ID", -1)
    }

}