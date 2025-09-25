package com.example.penny_pimpin.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.penny_pimpin.data.model.CategoryEntity

@Dao
interface CategoryDao {

    @Insert
    suspend fun insertCategory(category: CategoryEntity)

    @Query("SELECT * FROM categories WHERE userId = :userId")
    fun getCategoriesByUserId(userId: Int): LiveData<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE userId = :userId")
    suspend fun getAllCategoriesForUser(userId: Int): List<CategoryEntity>

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("SELECT name FROM categories WHERE id = :categoryId")
    suspend fun getCategoryName(categoryId: Int): String?
}