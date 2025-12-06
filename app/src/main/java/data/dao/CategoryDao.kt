package data.dao

import androidx.room.*
import data.entities.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Int): Category?

    @Query("SELECT * FROM categories WHERE type = :type AND is_active = 1 ORDER BY sort_order")
    fun getCategoriesByType(type: String): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE is_active = 1 ORDER BY sort_order")
    fun getAllActiveCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories ORDER BY sort_order")
    suspend fun getAllCategories(): List<Category>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
}