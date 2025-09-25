package com.example.penny_pimpin.data.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.lifecycle.LiveData
import androidx.room.OnConflictStrategy
import com.example.penny_pimpin.data.model.CategoryBudgetGoalEntity

@Dao
interface CategoryBudgetGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(goal: CategoryBudgetGoalEntity)

    @Query("SELECT * FROM category_budget_goals WHERE userId = :userId AND categoryId = :categoryId AND month = :month LIMIT 1")
    suspend fun getGoalForCategory(userId: Int, categoryId: Int, month: String): CategoryBudgetGoalEntity?

    @Query("SELECT * FROM category_budget_goals WHERE userId = :userId AND month = :month")
    suspend fun getAllGoalsForUserInMonth(userId: Int, month: String): List<CategoryBudgetGoalEntity>

    @Query("SELECT * FROM category_budget_goals WHERE userId = :userId AND month BETWEEN :startMonth AND :endMonth")
    suspend fun getGoalsForUserAndPeriod(
        userId: Int,
        startMonth: String,
        endMonth: String
    ): List<CategoryBudgetGoalEntity>
}