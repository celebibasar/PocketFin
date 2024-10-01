package com.basarcelebi.pocketfin.screen

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.basarcelebi.pocketfin.R
import com.basarcelebi.pocketfin.data.Chat
import com.basarcelebi.pocketfin.data.ChatData
import com.basarcelebi.pocketfin.database.IncomeExpenseItem
import com.basarcelebi.pocketfin.database.PocketFinDatabase
import com.basarcelebi.pocketfin.ui.theme.Red500
import com.basarcelebi.pocketfin.ui.theme.Red700
import com.basarcelebi.pocketfin.ui.theme.VibrantGreen
import com.basarcelebi.pocketfin.viewmodel.HomeScreenViewModel
import com.basarcelebi.pocketfin.viewmodel.HomeScreenViewModel.Companion.getUserId
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(database: PocketFinDatabase, scope: CoroutineScope) {
    val homeScreenViewModel: HomeScreenViewModel = viewModel()
    val application: Application = LocalContext.current.applicationContext as Application
    var response = Chat("", null, false)

    // State Variables
    var totalAmount by remember { mutableDoubleStateOf(0.0) }
    var incomeAmount by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    var incomeDescription by remember { mutableStateOf("") }
    var expenseDescription by remember { mutableStateOf("") }
    var showIncomeDialog by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showAdviceDialog by remember { mutableStateOf(false) }
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black

    // Observing Lists
    val incomeList by homeScreenViewModel.incomeList.observeAsState(emptyList())
    val expenseList by homeScreenViewModel.expenseList.observeAsState(emptyList())

    // Calculating Total Amount
    LaunchedEffect(incomeList, expenseList) {
        val activeIncomeAmount = incomeList.filter { it.isActive }.sumOf { it.amount ?: 0.0 }
        val activeExpenseAmount = expenseList.filter { it.isActive }.sumOf { it.amount ?: 0.0 }
        totalAmount = activeIncomeAmount - activeExpenseAmount
    }

    LaunchedEffect(Unit) {
        homeScreenViewModel.refreshLists()
    }

    MaterialTheme(
        colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IncomeSection(
                    incomeList,
                    showIncomeDialog,
                    onShowDialogChange = { showIncomeDialog = it },
                    scope,
                    database
                )
                ExpenseSection(
                    expenseList,
                    showExpenseDialog,
                    onShowDialogChange = { showExpenseDialog = it },
                    scope,
                    database
                )
                TotalOverview(totalAmount, textColor)

                // Income Dialog
                if (showIncomeDialog) {
                    IncomeDialog(
                        incomeDescription = incomeDescription,
                        incomeAmount = incomeAmount,
                        onIncomeDescriptionChange = { incomeDescription = it },
                        onIncomeAmountChange = { incomeAmount = it },
                        onDismiss = { showIncomeDialog = false },
                        onConfirm = {
                            addIncomeItem(
                                incomeDescription,
                                incomeAmount,
                                scope,
                                database,
                                homeScreenViewModel
                            )
                            showIncomeDialog = false
                            incomeAmount = ""
                            incomeDescription = ""
                        }
                    )
                }

                // Expense Dialog
                if (showExpenseDialog) {
                    ExpenseDialog(
                        expenseDescription = expenseDescription,
                        expenseAmount = expenseAmount,
                        onExpenseDescriptionChange = { expenseDescription = it },
                        onExpenseAmountChange = { expenseAmount = it },
                        onDismiss = { showExpenseDialog = false },
                        onConfirm = {
                            addExpenseItem(
                                expenseDescription,
                                expenseAmount,
                                scope,
                                database,
                                homeScreenViewModel
                            )
                            showExpenseDialog = false
                            expenseAmount = ""
                            expenseDescription = ""
                        }
                    )
                }

                // Advice Dialog
                if (showAdviceDialog) {
                    AdviceDialog(
                        response,
                        onDismiss = { showAdviceDialog = false },
                        isDarkTheme = isDarkTheme,
                        textColor = textColor
                    )
                }
            }
            AdviceButton(
                application = application,
                response = response,
                showAdviceDialog = showAdviceDialog,
                onShowDialogChange = { showAdviceDialog = it },
                onResponseChange = { newResponse -> response = newResponse },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp) // Optional: Add padding if needed
            )
        }

        // Advice Dialog
        if (showAdviceDialog) {
            AdviceDialog(response, onDismiss = { showAdviceDialog = false }, isDarkTheme = isDarkTheme, textColor = textColor)
        }
    }
}

@Composable
fun IncomeSection(incomeList: List<IncomeExpenseItem>, showIncomeDialog: Boolean, onShowDialogChange: (Boolean) -> Unit, scope: CoroutineScope, database: PocketFinDatabase) {
    Text(
        text = "Incomes",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = VibrantGreen
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        items(incomeList) { item ->
            IncomeExpenseItemRow(item = item, scope = scope, database = database)
        }
    }

    Button(
        onClick = { onShowDialogChange(true) },
        modifier = Modifier.padding(vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = VibrantGreen,
            contentColor = MaterialTheme.colorScheme.onSecondary
        )
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Text("Add Income", color = MaterialTheme.colorScheme.onSecondary, style = TextStyle(fontWeight = FontWeight.Bold))
    }
}

@Composable
fun ExpenseSection(expenseList: List<IncomeExpenseItem>, showExpenseDialog: Boolean, onShowDialogChange: (Boolean) -> Unit, scope: CoroutineScope, database: PocketFinDatabase) {
    Text(
        text = "Expenses",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = Red500
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        items(expenseList) { item ->
            IncomeExpenseItemRow(item = item, scope = scope, database = database)
        }
    }

    Button(
        onClick = { onShowDialogChange(true) },
        modifier = Modifier.padding(vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Red500,
            contentColor = MaterialTheme.colorScheme.onSecondary
        )
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Text("Add Expense", color = MaterialTheme.colorScheme.onSecondary, style = TextStyle(fontWeight = FontWeight.Bold))
    }
}

@Composable
fun TotalOverview(totalAmount: Double, textColor: Color) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Total Overview",
        fontSize = 32.sp,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(vertical = 8.dp),
        color = textColor
    )

    Text(
        text = "€${String.format("%.2f", totalAmount)}",
        fontSize = 24.sp,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = if (totalAmount >= 0) VibrantGreen else Red500
    )
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun AdviceButton(
    application: Application,
    response: Chat,
    showAdviceDialog: Boolean,
    onShowDialogChange: (Boolean) -> Unit,
    onResponseChange: (Chat) -> Unit,
    modifier: Modifier = Modifier // Add modifier parameter
) {
    SmallFloatingActionButton(
        onClick = {
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val newResponse = ChatData.getResponseAndAdvice(application)
                    onResponseChange(newResponse)
                    onShowDialogChange(true)
                } catch (e: Exception) {
                    onResponseChange(Chat("Error", null, false))
                    onShowDialogChange(true)
                }
            }
        },
        modifier = modifier.size(50.dp),
        shape = CircleShape,
        containerColor = VibrantGreen
    ) {
        Icon(
            painter = painterResource(id = R.drawable.gemini),
            modifier = Modifier.size(24.dp),
            contentDescription = "content description",
            tint = MaterialTheme.colorScheme.surface
        )
    }
}


private fun addIncomeItem(description: String, amount: String, scope: CoroutineScope, database: PocketFinDatabase, viewModel: HomeScreenViewModel) {
    val userId = getUserId() ?: return
    val newItem = IncomeExpenseItem(
        userId = userId,
        description = description,
        amount = amount.replace(",", ".").replace("-", "").replace("+", "").toDoubleOrNull() ?: 0.0,
        isActive = true,
        type = "income"
    )
    scope.launch {
        withContext(scope.coroutineContext) {
            database.incomeExpenseDao().insertIncomeItem(newItem)
            viewModel.refreshLists()
        }
    }
}

private fun addExpenseItem(description: String, amount: String, scope: CoroutineScope, database: PocketFinDatabase, viewModel: HomeScreenViewModel) {
    val userId = HomeScreenViewModel.getUserId() ?: return
    val newItem = IncomeExpenseItem(
        userId = userId,
        description = description,
        amount = amount.replace(",", ".").replace("-", "").replace("+", "").toDoubleOrNull() ?: 0.0,
        isActive = true,
        type = "expense"
    )
    scope.launch {
        withContext(Dispatchers.IO) {
            database.incomeExpenseDao().insertExpenseItem(newItem)
        }
        viewModel.refreshLists()
    }
}

@Composable
fun AdviceDialog(response: Chat, onDismiss: () -> Unit, isDarkTheme: Boolean, textColor: Color) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
                .heightIn(max = 400.dp)
                .widthIn(max = 300.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDarkTheme) Color.DarkGray else Color.White)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Response",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = response.prompt
                            .replace("*", "")
                            .replace("\n\n", "\n")
                            .replace("\n", "\n\n"),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.padding(8.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = VibrantGreen)
                    ) {
                        Text(
                            "OK",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.surface
                        )
                    }
                }
            }
        }
    }
}



@Composable
private fun IncomeExpenseItemRow(
    item: IncomeExpenseItem,
    scope: CoroutineScope,
    database: PocketFinDatabase
) {
    val homeScreenViewModel: HomeScreenViewModel = viewModel()
    val itemColor = if (item.type == "income") VibrantGreen else Red700
    val checkboxColor = if (item.type == "income") VibrantGreen else Red700
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isActive,
            onCheckedChange = { isChecked ->
                val updatedItem = item.copy(isActive = isChecked)
                scope.launch {
                    withContext(Dispatchers.IO) {
                        if (item.type == "income") {
                            database.incomeExpenseDao().updateIncomeItem(updatedItem)
                        } else {
                            database.incomeExpenseDao().updateExpenseItem(updatedItem)
                        }
                    }
                    // Refresh lists after update
                    homeScreenViewModel.refreshLists()
                }
            },
            colors = CheckboxDefaults.colors(checkedColor = checkboxColor, uncheckedColor = textColor)
        )
        Column {

            Text(text = item.description, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = textColor)
            Text(
                text = "€${String.format("%.2f", item.amount ?: 0.0)}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = itemColor
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { isDropdownExpanded = !isDropdownExpanded }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = textColor)
                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(onClick = {
                        isDropdownExpanded = false
                        showDialog = true
                    },modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = textColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = textColor)
                    }
                    DropdownMenuItem(onClick = {
                        isDropdownExpanded = false
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                database.incomeExpenseDao().deleteItem(item)
                            }
                            homeScreenViewModel.refreshLists()
                        }
                    },modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                        Icon(Icons.Default.Delete, contentDescription = "Edit", tint = textColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = textColor)
                    }
                }
            }
        }
    }

    if (showDialog) {
        if (item.type == "income")
            EditItemDialog(item = item, onDismiss = { showDialog = false }, database = database, scope = scope, homeScreenViewModel = homeScreenViewModel)
        else
            EditItemDialog(item = item, onDismiss = { showDialog = false }, database = database, scope = scope, homeScreenViewModel = homeScreenViewModel)
    }

}

@Composable
fun EditItemDialog(
    item: IncomeExpenseItem,
    onDismiss: () -> Unit,
    database: PocketFinDatabase,
    scope: CoroutineScope,
    homeScreenViewModel: HomeScreenViewModel
) {
    var description by remember { mutableStateOf(item.description) }
    var amount by remember { mutableStateOf(item.amount?.toString() ?: "") }
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val itemTypeColor = if (item.type == "income") VibrantGreen else Red700

    AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = MaterialTheme.colorScheme.surface,
        title = { Text("Edit Item",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = textColor, modifier = Modifier.padding(16.dp)) },
        text = {
            Column {
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Name", color = textColor) },
                    leadingIcon = {
                        Icon(painter = painterResource(id = R.drawable.description_24px), contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = textColor,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        placeholderColor = textColor,
                        focusedIndicatorColor = textColor,
                        unfocusedLabelColor = textColor,
                        focusedLabelColor = textColor,
                        leadingIconColor = textColor,
                        cursorColor = textColor
                    )
                )
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    leadingIcon = {
                        Icon(painter = painterResource(id = R.drawable.payments_24px), contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = textColor,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        placeholderColor = textColor,
                        focusedIndicatorColor = textColor,
                        unfocusedLabelColor = textColor,
                        focusedLabelColor = itemTypeColor,
                        leadingIconColor = itemTypeColor,
                        cursorColor = textColor
                    ),
                    isError = amount.toDoubleOrNull() == null
                )
                if(amount.toDoubleOrNull() == null) {
                    Text(
                        text = "Please enter a valid number",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Red700
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (amount.toDoubleOrNull() != null) {
                val updatedItem = item.copy(
                    description = description,
                    amount = amount.replace(",", ".").replace("-", "").replace("+", "")
                        .toDoubleOrNull() ?: 0.0
                )
                scope.launch {
                    withContext(Dispatchers.IO) {
                        if (item.type == "income") {
                            database.incomeExpenseDao().updateIncomeItem(updatedItem)
                        } else {
                            database.incomeExpenseDao().updateExpenseItem(updatedItem)
                        }
                    }
                    homeScreenViewModel.refreshLists()
                }
                onDismiss()
            }
            },colors = ButtonDefaults.buttonColors(
                backgroundColor = VibrantGreen,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(
                backgroundColor = Red700,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )) {
                Text("Cancel")
            }
        }
    )
}



@Composable
fun IncomeDialog(
    incomeDescription: String,
    incomeAmount: String,
    onIncomeDescriptionChange: (String) -> Unit,
    onIncomeAmountChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black
    AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Add Income",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = textColor, modifier = Modifier.padding(16.dp)
            )
        },
        text = {
            Column {
                TextField(
                    value = incomeDescription,
                    onValueChange = onIncomeDescriptionChange,
                    label = { Text("Name", color = textColor) },
                    leadingIcon = {
                        Icon(painter = painterResource(id = R.drawable.description_24px), contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = textColor,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        placeholderColor = textColor,
                        focusedIndicatorColor = textColor,
                        unfocusedLabelColor = textColor,
                        focusedLabelColor = textColor,
                        leadingIconColor = textColor,
                        cursorColor = textColor
                    )
                )
                TextField(
                    value = incomeAmount,
                    onValueChange = onIncomeAmountChange,
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    leadingIcon = {
                        Icon(painter = painterResource(id = R.drawable.payments_24px), contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = textColor,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        placeholderColor = textColor,
                        focusedIndicatorColor = textColor,
                        unfocusedLabelColor = textColor,
                        focusedLabelColor = VibrantGreen,
                        leadingIconColor = VibrantGreen,
                        cursorColor = textColor
                    ),
                    isError = incomeAmount.toDoubleOrNull() == null
                )
                if(incomeAmount.toDoubleOrNull() == null) {
                    Text(
                        text = "Please enter a valid number",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Red700
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(
                backgroundColor = VibrantGreen,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(
                backgroundColor = Red700,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ExpenseDialog(
    expenseDescription: String,
    expenseAmount: String,
    onExpenseDescriptionChange: (String) -> Unit,
    onExpenseAmountChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black
    AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Add Expense",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = textColor, modifier = Modifier.padding(16.dp)
            )
        },
        text = {
            Column {
                TextField(
                    value = expenseDescription,
                    onValueChange = onExpenseDescriptionChange,
                    label = { Text("Name") },
                    leadingIcon = {
                        Icon(painter = painterResource(id = R.drawable.description_24px), contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = textColor,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        placeholderColor = textColor,
                        focusedIndicatorColor = textColor,
                        unfocusedLabelColor = textColor,
                        focusedLabelColor = textColor,
                        leadingIconColor = textColor,
                        cursorColor = textColor
                    )
                )
                TextField(
                    value = expenseAmount,
                    onValueChange = onExpenseAmountChange,
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    leadingIcon = {
                        Icon(painter = painterResource(id = R.drawable.payments_24px), contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = textColor,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        placeholderColor = textColor,
                        focusedIndicatorColor = textColor,
                        unfocusedLabelColor = textColor,
                        focusedLabelColor = Red700,
                        leadingIconColor = Red700,
                        cursorColor = textColor
                    ),
                    isError = expenseAmount.toDoubleOrNull() == null
                )
                if(expenseAmount.toDoubleOrNull() == null) {
                    Text(
                        text = "Please enter a valid number",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Red700
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(
                backgroundColor = VibrantGreen,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(
                backgroundColor = Red700,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )) {
                Text("Cancel")
            }
        }
    )
}
