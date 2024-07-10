package com.basarcelebi.pocketfin.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.AutoMigration
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(entities = [IncomeExpenseItem::class], version = 4, exportSchema = false)
abstract class PocketFinDatabase : RoomDatabase() {

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
                    .build()
                INSTANCE = instance
                instance
            }
        }




    }
}

@Dao
interface IncomeExpenseDao {

    @Insert
    suspend fun insertIncomeItem(item: IncomeExpenseItem)

    @Insert
    suspend fun insertExpenseItem(item: IncomeExpenseItem)

    @Query("SELECT * FROM income_expense_items WHERE isActive = 1 AND type = 'income'")
    fun getAllActiveIncomeItems(): List<IncomeExpenseItem>

    @Query("SELECT * FROM income_expense_items WHERE isActive = 1 AND type = 'expense'")
    fun getAllActiveExpenseItems(): List<IncomeExpenseItem>

    @Query("SELECT SUM(amount) FROM income_expense_items WHERE isActive = 1 AND type = 'income'")
    fun getTotalIncome(): LiveData<Double>

    @Query("SELECT SUM(amount) FROM income_expense_items WHERE isActive = 1 AND type = 'expense'")
    fun getTotalExpense(): LiveData<Double>
}

@Entity(tableName = "income_expense_items")
data class IncomeExpenseItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "amount") val amount: Double?,
    @ColumnInfo(name = "isActive") var isActive: Boolean,
    @ColumnInfo(name = "type") val type: String
)






