package data.dao

import androidx.room.*
import data.entities.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account): Long

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Int): Account?

    @Query("SELECT * FROM accounts WHERE is_active = 1 ORDER BY sort_order")
    fun getAllActiveAccounts(): Flow<List<Account>>

    @Query("UPDATE accounts SET balance = balance + :amount WHERE id = :id")
    suspend fun updateBalance(id: Int, amount: Double)

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Int): Account?

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getAccountCount(): Int
}