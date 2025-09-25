package com.example.penny_pimpin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.penny_pimpin.data.model.CategoryTotal

class CategoryTotalAdapter(private val items: List<CategoryTotal>) :
    RecyclerView.Adapter<CategoryTotalAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryText: TextView = view.findViewById(R.id.category_name)
        val totalText: TextView = view.findViewById(R.id.category_total)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_total, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = items[position]
        holder.categoryText.text = item.categoryName
        holder.totalText.text = "R%.2f".format(item.total)
    }

    override fun getItemCount(): Int = items.size
}
