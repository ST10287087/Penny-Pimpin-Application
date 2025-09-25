package com.example.penny_pimpin
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.penny_pimpin.data.model.CategoryEntity


class CategoryAdapter(
    private val categories: List<CategoryEntity>,
    private val onCategoryClick: (CategoryEntity) -> Unit,
    private val onCategoryLongClick: (CategoryEntity) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.nameTextView.text = category.name
        holder.itemView.setOnClickListener {
            onCategoryClick(category) // Pass full object
        }
        // Apply dynamic background color
        val cardView = holder.itemView as androidx.cardview.widget.CardView
        cardView.setCardBackgroundColor(category.color)

        holder.itemView.setOnClickListener {
            onCategoryClick(category)
        }
        holder.itemView.setOnLongClickListener {
            onCategoryLongClick(category)
            true
        }
        Log.d("CategoryAdapter", "Binding category: ${category.name}")
    }

    override fun getItemCount(): Int {
        return categories.size
    }
}