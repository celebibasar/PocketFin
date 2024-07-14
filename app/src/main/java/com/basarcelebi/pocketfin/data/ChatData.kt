package com.basarcelebi.pocketfin.data

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.basarcelebi.pocketfin.database.IncomeExpenseDao
import com.basarcelebi.pocketfin.database.PocketFinDatabase
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ChatData {

    suspend fun getResponse(prompt: String):Chat
    {
        val generativeModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = com.basarcelebi.pocketfin.BuildConfig.apiKey)

        try {
            val response = withContext(Dispatchers.IO) {
                generativeModel.generateContent(prompt)
            }
            return Chat(prompt = response.text ?: "Error",
                bitmap = null,
                isFromUser = false
            )
        }catch (e: Exception)
        {
            return Chat(prompt = e.message ?: "Error",
                bitmap = null,
                isFromUser = false
            )

        }

    }

    suspend fun getResponseWithImage(prompt: String, bitmap: Bitmap):Chat
    {


        val generativeModel = GenerativeModel(
            modelName = "gemini-pro-vision",
            apiKey = com.basarcelebi.pocketfin.BuildConfig.apiKey
        )
        val inputContent = content{
            image(bitmap)
            text(prompt)
        }

        try {
            val response = withContext(Dispatchers.IO) {
                generativeModel.generateContent(inputContent)
            }
            return Chat(prompt = response.text ?: "Error",
                bitmap = null,
                isFromUser = false
            )
        }catch (e: Exception)
        {
            return Chat(prompt = e.message ?: "Error",
                bitmap = null,
                isFromUser = false
            )

        }

    }

    suspend fun getResponseAndAdvice(application: Application): Chat {
        val generativeModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = com.basarcelebi.pocketfin.BuildConfig.apiKey
        )
        val incomeExpenseDao: IncomeExpenseDao = PocketFinDatabase.getDatabase(application).incomeExpenseDao()
        var userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser


        val initialPrompt = "I am ${user?.displayName}. Give me recommendation about my financial situation."




        try {
            val incomeItems = incomeExpenseDao.getAllIncomeItems(userId)
            val expenseItems = incomeExpenseDao.getAllExpenseItems(userId)

            val totalIncome = incomeItems.sumOf { it.amount ?: 0.0 }
            val totalExpense = expenseItems.sumOf { it.amount ?: 0.0 }


            val incomeDescriptions = incomeItems.joinToString(separator = ", ") { it.description }
            val expenseDescriptions = expenseItems.joinToString(separator = ", ") { it.description }

            val inputPrompt = "Incomes: $incomeDescriptions (Sum of Income: $totalIncome), \n\n Expenses: $expenseDescriptions (Sum of Expenses: $totalExpense) \n" +
                    "\n $initialPrompt"

            val response = withContext(Dispatchers.IO) {
                generativeModel.generateContent(inputPrompt)
            }

            return Chat(
                prompt = response.text ?: "Error",
                bitmap = null,
                isFromUser = false
            )
        } catch (e: Exception) {
            return Chat(
                prompt = e.message ?: "Error",
                bitmap = null,
                isFromUser = false
            )
        }
    }

}

