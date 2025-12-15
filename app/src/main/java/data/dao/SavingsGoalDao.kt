package data.dao

import androidx.room.*
import data.entities.SavingsGoal

@Dao
interface SavingsGoalDao {

    @Insert
    suspend fun insert(goal: SavingsGoal): Long

    @Update
    suspend fun update(goal: SavingsGoal)

    @Delete
    suspend fun delete(goal: SavingsGoal)

    @Query("SELECT * FROM savings_goals ORDER BY is_completed ASC, created_at DESC")
    suspend fun getAll(): List<SavingsGoal>

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getById(id: Int): SavingsGoal?

    @Query("SELECT COALESCE(SUM(target_amount), 0) FROM savings_goals WHERE is_completed = 0")
    suspend fun getTotalTargetAmount(): Double

    @Query("SELECT COALESCE(SUM(current_amount), 0) FROM savings_goals WHERE is_completed = 0")
    suspend fun getTotalCurrentAmount(): Double

    @Query("UPDATE savings_goals SET current_amount = :newAmount WHERE id = :id")
    suspend fun updateCurrentAmount(id: Int, newAmount: Double)

    @Query("UPDATE savings_goals SET is_completed = :isCompleted WHERE id = :id")
    suspend fun updateCompletionStatus(id: Int, isCompleted: Boolean)
}