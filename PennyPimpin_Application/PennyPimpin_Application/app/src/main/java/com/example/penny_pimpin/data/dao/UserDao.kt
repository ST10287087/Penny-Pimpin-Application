package com.example.penny_pimpin.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.penny_pimpin.data.model.UserEntity


@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserByEmail(email: String): UserEntity?
}