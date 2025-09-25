package com.example.penny_pimpin.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.penny_pimpin.data.dao.BudgetGoalDao
import com.example.penny_pimpin.data.dao.CategoryBudgetGoalDao
import com.example.penny_pimpin.data.dao.CategoryDao
import com.example.penny_pimpin.data.dao.ExpenseDao
import com.example.penny_pimpin.data.dao.LoanDao
import com.example.penny_pimpin.data.dao.UserDao
import com.example.penny_pimpin.data.model.BudgetGoalEntity
import com.example.penny_pimpin.data.model.CategoryBudgetGoalEntity
import com.example.penny_pimpin.data.model.CategoryEntity
import com.example.penny_pimpin.data.model.ExpenseEntity
import com.example.penny_pimpin.data.model.Loan
import com.example.penny_pimpin.data.model.UserEntity

@Database(entities = [UserEntity::class,ExpenseEntity::class,CategoryEntity::class, BudgetGoalEntity::class, CategoryBudgetGoalEntity::class, Loan::class], version = 8)
abstract class AppDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetGoalDao(): BudgetGoalDao
    abstract fun categoryBudgetGoalDao(): CategoryBudgetGoalDao
    abstract fun loanDao(): LoanDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}