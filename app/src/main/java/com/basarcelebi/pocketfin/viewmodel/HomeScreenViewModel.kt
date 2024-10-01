package com.basarcelebi.pocketfin.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.basarcelebi.pocketfin.database.IncomeExpenseDao
import com.basarcelebi.pocketfin.database.IncomeExpenseItem
import com.basarcelebi.pocketfin.database.PocketFinDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {

    // Database DAO
    private val incomeExpenseDao: IncomeExpenseDao = PocketFinDatabase.getDatabase(application).incomeExpenseDao()

    // LiveData for income and expense lists
    private val _incomeList = MutableLiveData<List<IncomeExpenseItem>>()
    val incomeList: LiveData<List<IncomeExpenseItem>>
        get() = _incomeList

    private val _expenseList = MutableLiveData<List<IncomeExpenseItem>>()
    val expenseList: LiveData<List<IncomeExpenseItem>>
        get() = _expenseList

    // LiveData for observing total amount
    private val _totalAmount = MediatorLiveData<Double>()
    val totalAmount: LiveData<Double>
        get() = _totalAmount

    private lateinit var userId: String // User ID, Firebase Authentication'dan alınacak

    init {
        // Get the current user ID from Firebase Authentication
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // LiveData'ları ve totalAmount'i başlat
        refreshLists()
        calculateTotal()
    }

    // Calculate total income and expense
    private fun calculateTotal() {
        // Retrieve total income and expenses from LiveData sources
        val liveDataIncome = incomeExpenseDao.getTotalIncome(userId)
        val liveDataExpense = incomeExpenseDao.getTotalExpense(userId)

        // Observe changes in total income
        _totalAmount.addSource(liveDataIncome) { income ->
            // Filter active income items and calculate total income
            val activeIncome = _incomeList.value?.filter { it.isActive } ?: emptyList()
            val totalIncome = activeIncome.sumOf { it.amount ?: 0.0 }
            val activeExpense = _expenseList.value?.filter { it.isActive } ?: emptyList()
            val totalExpense = activeExpense.sumOf { it.amount ?: 0.0 }
            _totalAmount.value = totalIncome - totalExpense
        }

    }




    // Refresh income and expense lists
    fun refreshLists() {
        viewModelScope.launch {
            // Perform database operations in IO dispatcher
            val incomeItems = withContext(Dispatchers.IO) {
                incomeExpenseDao.getAllIncomeItems(userId)
            }
            _incomeList.value = incomeItems

            val expenseItems = withContext(Dispatchers.IO) {
                incomeExpenseDao.getAllExpenseItems(userId)
            }
            _expenseList.value = expenseItems
        }
    }

    // Add a new income item
    fun addIncomeItem(description: String, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val newItem = IncomeExpenseItem(userId = userId, description = description, amount = amount, isActive = true, type = "income")
            incomeExpenseDao.insertIncomeItem(newItem)
            refreshLists() // Refresh lists to include the new item
        }
    }

    // Add a new expense item
    fun addExpenseItem(description: String, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val newItem = IncomeExpenseItem(userId = userId, description = description, amount = amount, isActive = true, type = "expense")
            incomeExpenseDao.insertExpenseItem(newItem)
            refreshLists() // Refresh lists to include the new item
        }
    }

    companion object {
        fun getUserId(): String? {
            val currentUser = FirebaseAuth.getInstance().currentUser
            return currentUser?.uid

        }
    }
}
