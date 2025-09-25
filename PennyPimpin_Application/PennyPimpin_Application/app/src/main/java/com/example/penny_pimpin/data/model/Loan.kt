package com.example.penny_pimpin.data.model
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class Loan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val interestRate: Double,
    val startDate: String,
    val durationMonths: Int,
    val monthlyRepayment: Double
)