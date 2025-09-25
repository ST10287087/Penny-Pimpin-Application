package com.example.penny_pimpin.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.penny_pimpin.R
import com.example.penny_pimpin.data.model.Loan

class LoanAdapter(private val loanList: List<Loan>) : RecyclerView.Adapter<LoanAdapter.LoanViewHolder>() {

    class LoanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStartDate: TextView = itemView.findViewById(R.id.tvStartDate)
        val tvLoanAmount: TextView = itemView.findViewById(R.id.tvLoanAmount)
        val tvLoanDetails: TextView = itemView.findViewById(R.id.tvLoanDetails)
        val tvMonthly: TextView = itemView.findViewById(R.id.tvMonthly)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loan, parent, false)
        return LoanViewHolder(view)
    }

    override fun onBindViewHolder(holder: LoanViewHolder, position: Int) {
        val loan = loanList[position]
        /*
        Attribution:
        Website: How to Change the Whole App Language in Android Programmatically? - GeeksForGeeks
        Author: GeeksForGeeks
        URL:https://www.geeksforgeeks.org/how-to-change-the-whole-app-language-in-android-programmatically/
        Accessed on: 2025-06-04
         */
        when (UserSession.langu) {
            "af" -> {
                holder.tvStartDate.text = "Begindatum: ${loan.startDate}"
                holder.tvLoanAmount.text = "Lening: R%.2f".format(loan.amount)
                holder.tvLoanDetails.text = "Rentekoers: %.2f%%, Tydperk: %d maande".format(loan.interestRate, loan.durationMonths)
                holder.tvMonthly.text = "Maandelikse Terugbetaling: R%.2f".format(loan.monthlyRepayment)
            }
            "zu" -> {
                holder.tvStartDate.text = "Usuku lokuqala: ${loan.startDate}"
                holder.tvLoanAmount.text = "Imalimboleko: R%.2f".format(loan.amount)
                holder.tvLoanDetails.text = "Inzalo: %.2f%%, Isikhathi: izinyanga %d".format(loan.interestRate, loan.durationMonths)
                holder.tvMonthly.text = "Inkokhelo yanyanga zonke: R%.2f".format(loan.monthlyRepayment)
            }
            else -> { // English (default)
                holder.tvStartDate.text = "Start Date: ${loan.startDate}"
                holder.tvLoanAmount.text = "Loan: R%.2f".format(loan.amount)
                holder.tvLoanDetails.text = "Rate: %.2f%%, Duration: %d months".format(loan.interestRate, loan.durationMonths)
                holder.tvMonthly.text = "Monthly Repayment: R%.2f".format(loan.monthlyRepayment)
            }
        }
    }


    override fun getItemCount() = loanList.size // Return item count
}