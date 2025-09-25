package com.example.penny_pimpin
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.penny_pimpin.data.model.ExpenseEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


interface ExpenseActionListener {
    fun onEditExpense(expense: ExpenseEntity)
    fun onDeleteExpense(expense: ExpenseEntity)
}
class ExpenseAdapter(
    private var expenses: List<ExpenseEntity>,
    private val listener: ExpenseActionListener
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.textExpenseName)
        val amount: TextView = itemView.findViewById(R.id.textExpenseAmount)
        val date: TextView = itemView.findViewById(R.id.textExpenseDate)
        val description: TextView = itemView.findViewById(R.id.textExpenseDescription)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onEditExpense(expenses[position])
                }
            }

            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeleteExpense(expenses[position])
                    true
                } else false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }



    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.name.text = expense.name
        holder.amount.text = "R${expense.amount}"
        val millis = expense.startDateTime.toLongOrNull() ?: 0L

        // Format the timestamp to a readable date format (e.g., "dd-MM-yyyy")
        val date = Date(millis)
        val formattedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date)
        holder.date.text = formattedDate
        holder.description.text = expense.description

    }

    override fun getItemCount(): Int = expenses.size

    fun updateExpenses(newExpenses: List<ExpenseEntity>) {
        expenses = newExpenses
        notifyDataSetChanged()  // Notify the adapter that the data has changed
    }
}
