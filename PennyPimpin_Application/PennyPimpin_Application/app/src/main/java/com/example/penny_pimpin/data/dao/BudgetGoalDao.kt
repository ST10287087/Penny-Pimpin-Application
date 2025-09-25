package com.example.penny_pimpin.data.dao

import androidx.room.Dao

import androidx.room.Insert
import androidx.room.Query

import androidx.room.OnConflictStrategy
import com.example.penny_pimpin.data.model.BudgetGoalEntity

@Dao
interface BudgetGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: BudgetGoalEntity)

    @Query("SELECT * FROM budget_goals WHERE userId = :userId LIMIT 1")
    suspend fun getGoalForUser(userId: Int): BudgetGoalEntity?
}