package com.example.penny_pimpin.fragment

import UserSession
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penny_pimpin.CategoryTotalAdapter
import com.example.penny_pimpin.R
import com.example.penny_pimpin.data.database.AppDatabase
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
/*
Attribution:
Video:
charts – YouTube.
Author/Channel: Andriod geek
Title: "Working with Charts in Kotlin"
URL: https://youtu.be/4ou5yRJtuKU?si=5IY0NveE6IHCTqFD
Accessed on: 2025-04-30
 */
class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
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
        val expenseDao = db.expenseDao()
        val sharedPref = requireContext().getSharedPreferences("userSession", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)
        val textwelcome = view.findViewById<TextView>(R.id.textwelcome)
        val monthlyText = view.findViewById<TextView>(R.id.monthlyText)
        val budgetTitle = view.findViewById<TextView>(R.id.budgetTitle)
        val cattitle = view.findViewById<TextView>(R.id.cattitle)
        val Expensetitle = view.findViewById<TextView>(R.id.Expensetitle)

        if (UserSession.langu == "en") {
            textwelcome.text = "Welcome Back"
            monthlyText.text = "Monthly Feedback"
            budgetTitle.text = "Budget Usage Overview"
            cattitle.text = "Category Expense Distribution"
            Expensetitle.text = "Expense Summary by Category"
        } else if (UserSession.langu == "af") {
            textwelcome.text = "Welkom Terug"
            monthlyText.text = "Maandelikse Terugvoer"
            budgetTitle.text = "Begrotinggebruik Oorsig"
            cattitle.text = "Uitgaweverspreiding per Kategorie"
            Expensetitle.text = "Uitgawe Opsomming per Kategorie"
        } else if (UserSession.langu == "zu") {
            textwelcome.text = "Siyakwamukela Emuva"
            monthlyText.text = "Impendulo Yenyanga"
            budgetTitle.text = "Uhlolojikelele Lokusetshenziswa Kwebhajethi"
            cattitle.text = "Ukwabiwa Kwezindleko Ngokuqondene Nekhethegori"
            Expensetitle.text = "Isifinyezo Sezindleko Ngokwezigaba"
        }



        // val pieChart = view.findViewById<PieChart>(R.id.pieChart)
        // Load and display budget progress and status
        lifecycleScope.launch {

            val progressBar = view.findViewById<ProgressBar>(R.id.budgetProgressBar)
            val totalVsGoal = view.findViewById<TextView>(R.id.textTotalVsGoal)
            val pieChartBudgetSummary = view.findViewById<PieChart>(R.id.pieChartBudgetSummary)

            val totalSpent = db.expenseDao().getTotalExpenses(userId) ?: 0.0
            val goalEntity = db.budgetGoalDao().getGoalForUser(userId)
            val income = goalEntity?.income ?: 0.0

            val month = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

            // Fetch all categories for the user
            val categories = withContext(Dispatchers.IO) {
                db.categoryDao().getAllCategoriesForUser(userId)
            }

            var totalCategoryBudget = 0.0

            // Calculate total of all category goals
            withContext(Dispatchers.IO) {
                for (cat in categories) {
                    val catGoal = db.categoryBudgetGoalDao().getGoalForCategory(userId, cat.id, month)
                    if (catGoal != null) {
                        totalCategoryBudget += catGoal.goalAmount
                    }
                }
            }
            val statusText = view.findViewById<TextView>(R.id.textBudgetGoalStatus)
            val pair = when {
                totalCategoryBudget == 0.0 -> Pair(
                    when (UserSession.langu) {
                        "af" -> "Geen Begrotingsdoelwitte Gestel Nie"
                        "zu" -> "Ayikho imigomo yeBhajethi esethwe"
                        else -> "No Budget Goals Set"
                    },
                    Color.GRAY
                )
                totalSpent < totalCategoryBudget * 0.8 -> Pair(
                    when (UserSession.langu) {
                        "af" -> "Jy is onder begroting!"
                        "zu" -> "Usebudget kahle!"
                        else -> "You're under budget!"
                    },
                    Color.parseColor("#4CAF50")
                )
                totalSpent <= totalCategoryBudget -> Pair(
                    when (UserSession.langu) {
                        "af" -> "Jy is op dreef!"
                        "zu" -> "Usemkhondo omuhle!"
                        else -> "You're on track!"
                    },
                    Color.parseColor("#FFC107")
                )
                else -> Pair(
                    when (UserSession.langu) {
                        "af" -> "Jy is oor begroting!"
                        "zu" -> "Udlulile ebhajethini!"
                        else -> "You're over budget!"
                    },
                    Color.parseColor("#F44336")
                )
            }


            statusText.text = pair.first
            statusText.setTextColor(pair.second)

            // Calculate and update progress bar
            val progress = if (totalCategoryBudget > 0) {
                ((totalSpent / totalCategoryBudget) * 100).toInt().coerceAtMost(100)
            } else {
                0
            }

            progressBar.progress = progress
            val percentageText = view.findViewById<TextView>(R.id.tvBudgetPercentage)
            percentageText.text = "$progress%"

            val totalVsGoalText = when (UserSession.langu) {
                "af" -> "Totale Uitgawes: R%.2f / Begroting: R%.2f".format(totalSpent, totalCategoryBudget)
                "zu" -> "Izindleko Ezisondele: R%.2f / Isabelomali: R%.2f".format(totalSpent, totalCategoryBudget)
                else -> "Total Expenses: R%.2f / Budget: R%.2f".format(totalSpent, totalCategoryBudget)
            }

            totalVsGoal.text = totalVsGoalText


            if (totalCategoryBudget > 0) {
                pieChartBudgetSummary.visibility = View.VISIBLE
                setupBudgetPieChart(pieChartBudgetSummary, totalSpent, totalCategoryBudget)
            } else {
                pieChartBudgetSummary.visibility = View.GONE
            }

            // Display budget status
            /*statusText.text = when {
                totalCategoryBudget == 0.0 -> "No Budget Goals Set"
                totalSpent > totalCategoryBudget -> "Status: Over Budget"
                else -> "Status: Looking Good!"
            }*/

            val feedbackText = view.findViewById<TextView>(R.id.tvBudgetFeedback)

            if (totalCategoryBudget == 0.0) {
                val noBudgetText = when (UserSession.langu) {
                    "af" -> "Geen begroting is gestel vir hierdie maand nie."
                    "zu" -> "Akukho mbhalo wesabelomali wesinyanga le."
                    else -> "No budget set for this month."
                }
                feedbackText.text = noBudgetText
                feedbackText.setTextColor(Color.GRAY)
            } else {
                val percentage = (totalSpent / totalCategoryBudget) * 100

                when {
                    percentage < 100 -> {
                        val underBudgetText = when (UserSession.langu) {
                            "af" -> "Jy is ${"%.0f".format(100 - percentage)}% onder die begroting!"
                            "zu" -> "Use ${"%.0f".format(100 - percentage)}% ngaphansi kwesabelomali!"
                            else -> "You're ${"%.0f".format(100 - percentage)}% under budget!"
                        }
                        feedbackText.text = underBudgetText
                        feedbackText.setTextColor(Color.parseColor("#4CAF50")) // Green
                    }
                    percentage == 100.0 -> {
                        val onBudgetText = when (UserSession.langu) {
                            "af" -> "Jy is presies op die begroting."
                            "zu" -> "Use sesimeni ku-isabelomali."
                            else -> "You're exactly on budget."
                        }
                        feedbackText.text = onBudgetText
                        feedbackText.setTextColor(Color.parseColor("#FFC107")) // Amber
                    }
                    percentage > 100 -> {
                        val overBudgetText = when (UserSession.langu) {
                            "af" -> "Jy het die begroting oorskry!"
                            "zu" -> "Udlulile esabelomali!"
                            else -> "You've gone over budget!"
                        }
                        feedbackText.text = overBudgetText
                        feedbackText.setTextColor(Color.parseColor("#F44336")) // Red
                    }
                }
            }





        }

        // Load and display category-wise expense totals
        CoroutineScope(Dispatchers.IO).launch {
            val categoryTotals = expenseDao.getCategoryTotals(userId)
            val totalExpenses = expenseDao.getTotalExpenses(userId)

            withContext(Dispatchers.Main) {
                val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_category_totals)
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = CategoryTotalAdapter(categoryTotals)

                try {
                    val pieChart = view.findViewById<PieChart>(R.id.pieChart)

                    if (categoryTotals.isNullOrEmpty() || categoryTotals.all { it.total == 0.0 }) {
                        // No data: Hide the pie chart
                        pieChart.visibility = View.GONE
                    } else {
                        // Prepare pie chart entries
                        val entries = categoryTotals
                            .filter { it.total > 0 }
                            .map { PieEntry(it.total.toFloat(), it.categoryName) }

                        if (entries.isNotEmpty()) {
                            val label = when (UserSession.langu) {
                                "af" -> "Uitgawes per Kategorie"
                                "zu" -> "Izindleko ngeSigaba"
                                else -> "Expenses by Category"
                            }

                            val dataSet = PieDataSet(entries, label).apply {
                                colors = com.github.mikephil.charting.utils.ColorTemplate.MATERIAL_COLORS.toList()
                                valueTextColor = Color.BLACK
                                valueTextSize = 12f
                            }

                            val pieData = PieData(dataSet)
                            pieData.setValueFormatter(PercentFormatter(pieChart))
                            pieChart.apply {
                                data = PieData(dataSet)

                                description.isEnabled = false
                                setUsePercentValues(true)
                                setEntryLabelColor(Color.BLACK)
                                setDrawEntryLabels(true)
                                setDrawHoleEnabled(true)
                                holeRadius = 40f
                                transparentCircleRadius = 45f
                                setHoleColor(Color.TRANSPARENT)
                                setExtraOffsets(5f, 5f, 5f, 5f)
                                legend.orientation = Legend.LegendOrientation.VERTICAL
                                legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                                legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
                                legend.isWordWrapEnabled = true
                                visibility = View.VISIBLE
                                invalidate()
                            }
                        } else {
                            pieChart.visibility = View.GONE
                        }
                    }
                } catch (e: Exception) {
                    val errorMessage = when (UserSession.langu) {
                        "af" -> "Fout tydens die vertoon van die sirkelgrafiek: ${e.message}"
                        "zu" -> "Iphutha ekuboneni ishadi lesiyingi: ${e.message}"
                        else -> "Error displaying pie chart: ${e.message}"
                    }
                    android.util.Log.e("PieChartError", errorMessage, e)
                }

            }
        }



    }
    private fun setupBudgetPieChart(pieChart: PieChart, totalSpent: Double, totalBudget: Double) {
        val entries = ArrayList<PieEntry>()

        if (totalBudget > 0) {
            val spent = totalSpent.toFloat()
            val remaining = (totalBudget - totalSpent).toFloat().coerceAtLeast(0f)

            val spentLabel = when (UserSession.langu) {
                "af" -> "Bestee"
                "zu" -> "Kusetshenzisiwe"
                else -> "Spent"
            }
            val remainingLabel = when (UserSession.langu) {
                "af" -> "Oorblywend"
                "zu" -> "Sekusele"
                else -> "Remaining"
            }

            entries.add(PieEntry(spent, spentLabel))
            entries.add(PieEntry(remaining, remainingLabel))
        } else {
            val noBudgetLabel = when (UserSession.langu) {
                "af" -> "Geen Begroting"
                "zu" -> "Akukho Bhajethi"
                else -> "No Budget"
            }
            entries.add(PieEntry(1f, noBudgetLabel))
        }


        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            Color.parseColor("#F44336"), // Red for Spent
            Color.parseColor("#4CAF50")  // Green for Remaining
        )
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 14f

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))
        pieChart.apply {
            this.data = data
            description.isEnabled = false
            setUsePercentValues(true)
            setEntryLabelColor(Color.BLACK)
            setDrawEntryLabels(true)
            setDrawHoleEnabled(true)
            holeRadius = 40f
            transparentCircleRadius = 45f
            setHoleColor(Color.TRANSPARENT)
            setExtraOffsets(5f, 5f, 5f, 5f)
            legend.orientation = Legend.LegendOrientation.VERTICAL
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
            legend.isWordWrapEnabled = true
            centerText = "This Month"
            setCenterTextColor(Color.WHITE)
            setCenterTextSize(14f)
            animateY(1000)
            invalidate()
        }
    }
}
