package com.basarcelebi.pocketfin.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.basarcelebi.pocketfin.database.IncomeExpenseDao
import com.basarcelebi.pocketfin.database.IncomeExpenseItem
import com.basarcelebi.pocketfin.database.PocketFinDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val incomeExpenseDao: IncomeExpenseDao = PocketFinDatabase.getDatabase(application).incomeExpenseDao()

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

    private val totalIncome: LiveData<Double> = incomeExpenseDao.getTotalIncome()
    private val totalExpense: LiveData<Double> = incomeExpenseDao.getTotalExpense()

    init {
        // Initialize data
        refreshLists()
        calculateTotal()
    }

    private fun calculateTotal() {
        _totalAmount.addSource(totalIncome) { income ->
            val expense = totalExpense.value ?: 0.0
            _totalAmount.value = (income ?: 0.0) - expense
        }
        _totalAmount.addSource(totalExpense) { expense ->
            val income = totalIncome.value ?: 0.0
            _totalAmount.value = income - (expense ?: 0.0)
        }
    }


    fun refreshLists() {
        viewModelScope.launch {
            // Perform database operations in IO dispatcher
            val incomeItems = withContext(Dispatchers.IO) {
                incomeExpenseDao.getAllActiveIncomeItems()
            }
            _incomeList.value = incomeItems

            val expenseItems = withContext(Dispatchers.IO) {
                incomeExpenseDao.getAllActiveExpenseItems()
            }
            _expenseList.value = expenseItems
        }
    }

    fun addIncomeItem(description: String, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val newItem = IncomeExpenseItem(description = description, amount = amount, isActive = true, type = "income")
            incomeExpenseDao.insertIncomeItem(newItem)
            refreshLists() // Refresh lists to include the new item
        }
    }

    fun addExpenseItem(description: String, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val newItem = IncomeExpenseItem(description = description, amount = amount, isActive = true, type = "expense")
            incomeExpenseDao.insertExpenseItem(newItem)
            refreshLists() // Refresh lists to include the new item
        }
    }
}
