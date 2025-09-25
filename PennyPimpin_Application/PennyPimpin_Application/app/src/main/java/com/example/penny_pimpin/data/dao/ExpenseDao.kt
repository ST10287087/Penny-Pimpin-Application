package com.example.penny_pimpin.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.penny_pimpin.data.model.CategoryTotal
import com.example.penny_pimpin.data.model.CategoryTotalRaw
import com.example.penny_pimpin.data.model.ExpenseEntity

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId")
    fun getExpensesByCategoryId(categoryId: Int): LiveData<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId AND  startDateTime BETWEEN :startDate AND :endDate")
    fun getExpensesByDateRange(categoryId: Int,startDate: Long, endDate: Long): LiveData<List<ExpenseEntity>>

   // @Query("SELECT categoryId, SUM(amount) as total FROM expenses WHERE userId = :userId GROUP BY categoryId")
   // fun getCategoryTotals(userId: Int): List<CategoryTotal>

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId")
    suspend fun getTotalExpenses(userId: Int): Double?

    @Query("""
    SELECT c.name AS categoryName, SUM(e.amount) AS total
    FROM expenses e
    INNER JOIN categories c ON e.categoryId = c.id
    WHERE e.userId = :userId
    GROUP BY c.name
""")
    fun getCategoryTotals(userId: Int): List<CategoryTotal>

    @Query("SELECT categoryId, SUM(amount) as totalAmount FROM expenses WHERE userId = :userId AND dateCreated BETWEEN :startDate AND :endDate GROUP BY categoryId")
    suspend fun getTotalSpentByCategory(
        userId: Int,
        startDate: String,
        endDate: String
    ): List<CategoryTotalRaw>


    @Query("SELECT * FROM expenses WHERE userId = :userId")
    suspend fun getAllExpensesForUser(userId: Int): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND dateCreated BETWEEN :startDate AND :endDate")
    suspend fun getExpensesInDateRange(userId: Int, startDate: String, endDate: String): List<ExpenseEntity>

}

