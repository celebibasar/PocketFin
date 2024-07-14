package com.basarcelebi.pocketfin.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update


@Database(entities = [User::class, IncomeExpenseItem::class], version = 6, exportSchema = false)
abstract class PocketFinDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun incomeExpenseDao(): IncomeExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: PocketFinDatabase? = null

        fun getDatabase(context: Context): PocketFinDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PocketFinDatabase::class.java,
                    "pocket_fin_database"
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUser(userId: String): User?
}

@Dao
interface IncomeExpenseDao {

    @Delete
    suspend fun deleteItem(item: IncomeExpenseItem)

    @Query("DELETE FROM income_expense_items")
    suspend fun deleteAllItems()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncomeItem(item: IncomeExpenseItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseItem(item: IncomeExpenseItem)

    @Update
    suspend fun updateIncomeItem(item: IncomeExpenseItem)

    @Update
    suspend fun updateExpenseItem(item: IncomeExpenseItem)

    @Query("SELECT * FROM income_expense_items WHERE user_id = :userId AND type = 'income'")
    fun getAllIncomeItems(userId: String): List<IncomeExpenseItem>

    @Query("SELECT * FROM income_expense_items WHERE user_id = :userId AND type = 'expense'")
    fun getAllExpenseItems(userId: String): List<IncomeExpenseItem>

    @Query("SELECT SUM(amount) FROM income_expense_items WHERE (user_id = :userId AND isActive = 1 AND type = 'income')")
    fun getTotalIncome(userId: String): LiveData<Double>

    @Query("SELECT SUM(amount) FROM income_expense_items WHERE (user_id = :userId AND isActive = 1 AND type = 'expense')")
    fun getTotalExpense(userId: String): LiveData<Double>
}

@Entity(tableName = "income_expense_items")
data class IncomeExpenseItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "amount") val amount: Double?,
    @ColumnInfo(name = "isActive") var isActive: Boolean,
    @ColumnInfo(name = "type") val type: String
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_name") val userName: String?
)
