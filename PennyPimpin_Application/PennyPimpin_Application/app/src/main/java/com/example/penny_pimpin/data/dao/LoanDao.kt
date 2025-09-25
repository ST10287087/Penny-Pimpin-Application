package com.example.penny_pimpin.data.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.penny_pimpin.data.model.Loan

@Dao
interface LoanDao {

    @Insert
    suspend fun insertLoan(loan: Loan)

    @Query("SELECT * FROM loans ORDER BY id DESC")
    suspend fun getAllLoans(): List<Loan>
}