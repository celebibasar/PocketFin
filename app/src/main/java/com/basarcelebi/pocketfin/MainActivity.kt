package com.basarcelebi.pocketfin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.basarcelebi.pocketfin.database.IncomeExpenseItem
import com.basarcelebi.pocketfin.database.PocketFinDatabase
import com.basarcelebi.pocketfin.screen.ProfileScreen
import com.basarcelebi.pocketfin.ui.theme.PocketFinTheme
import com.basarcelebi.pocketfin.viewmodel.HomeScreenViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var database: PocketFinDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = PocketFinDatabase.getDatabase(applicationContext)

        setContent {
            PocketFinTheme {
                PocketFinApp(database)
            }
        }
    }
}

@Composable
fun PocketFinApp(database: PocketFinDatabase) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        NavigationHost(navController, Modifier.padding(innerPadding), database, scope)
    }
}

@Composable
fun NavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    database: PocketFinDatabase,
    scope: CoroutineScope
) {
    NavHost(navController, startDestination = Screen.Home.route, modifier = modifier) {
        composable(Screen.Home.route) {
            HomeScreen(database, scope)
        }
        composable(Screen.AskToGemini.route) {
            ProfileScreen()
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    BottomNavigation {
        val items = listOf("home", "askToGemini", "profile")
        items.forEach { screen ->
            BottomNavigationItem(
                icon = {
                    when (screen) {
                        "home" -> Icon(Icons.Default.Home, contentDescription = "Home")
                        // Add icons for other screens as needed
                        else -> Icon(Icons.Default.Home, contentDescription = "Home")
                    }
                },
                label = { Text(screen) },
                selected = false, // Implement your own selection logic
                onClick = {
                    navController.navigate(screen) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
fun HomeScreen(database: PocketFinDatabase, scope: CoroutineScope) {
    val homeScreenViewModel: HomeScreenViewModel = viewModel()

    var totalAmount by remember { mutableStateOf(0.0) }
    var incomeAmount by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    var incomeDescription by remember { mutableStateOf("") }
    var expenseDescription by remember { mutableStateOf("") }
    var showIncomeDialog by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }

    // Observe income and expense lists and total amount
    val incomeList by homeScreenViewModel.incomeList.observeAsState(emptyList())
    val expenseList by homeScreenViewModel.expenseList.observeAsState(emptyList())
    val totalAmountLiveData by homeScreenViewModel.totalAmount.observeAsState(0.0)

    // Update totalAmount when totalAmountLiveData changes
    LaunchedEffect(incomeList, expenseList) {
        val activeIncomeAmount = incomeList.filter { it.isActive }.sumByDouble { it.amount!! }
        val activeExpenseAmount = expenseList.filter { it.isActive }.sumByDouble { it.amount!! }
        totalAmount = activeIncomeAmount - activeExpenseAmount
    }

    // Fetch initial data from the database
    LaunchedEffect(Unit) {
        homeScreenViewModel.refreshLists()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Income section
        Text("Incomes")
        incomeList.forEachIndexed { index, item ->
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
                                database.incomeExpenseDao().updateIncomeItem(updatedItem)
                            }
                            homeScreenViewModel.refreshLists()
                        }
                    }
                )


                Text("${item.description}: €${String.format("%.2f", item.amount)}")


            }
        }

        Button(
            onClick = { showIncomeDialog = true },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Add Income")
        }

        // Expenses section
        Spacer(modifier = Modifier.height(8.dp))
        Text("Expenses")
        expenseList.forEachIndexed { index, item ->
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
                                database.incomeExpenseDao().updateExpenseItem(updatedItem)
                            }
                            homeScreenViewModel.refreshLists()
                        }
                    }
                )


                Text(
                    text = "${item.description}: -€${String.format("%.2f", item.amount)}",
                )

            }
        }

        Button(
            onClick = { showExpenseDialog = true },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Add Expense")
        }

        // Total overview section
        Spacer(modifier = Modifier.height(16.dp))
        Text("Total Overview", style = MaterialTheme.typography.h6)
        Text("€${String.format("%.2f", totalAmount)}", style = MaterialTheme.typography.h4)

        // Dialogs
        if (showIncomeDialog) {
            IncomeDialog(
                incomeDescription = incomeDescription,
                incomeAmount = incomeAmount,
                onIncomeDescriptionChange = { incomeDescription = it },
                onIncomeAmountChange = { incomeAmount = it },
                onDismiss = { showIncomeDialog = false },
                onConfirm = {
                    val amount = incomeAmount.toDoubleOrNull() ?: 0.0
                    val userId = getUserId() ?: return@IncomeDialog
                    val newItem = IncomeExpenseItem(
                        userId = userId,
                        description = incomeDescription,
                        amount = amount,
                        isActive = true,
                        type = "income"
                    )
                    scope.launch {
                        withContext(scope.coroutineContext) {
                            database.incomeExpenseDao().insertIncomeItem(newItem)
                            homeScreenViewModel.refreshLists()
                        }
                    }
                    showIncomeDialog = false
                    incomeAmount = ""
                    incomeDescription = ""
                }
            )
        }

        if (showExpenseDialog) {
            ExpenseDialog(
                expenseDescription = expenseDescription,
                expenseAmount = expenseAmount,
                onExpenseDescriptionChange = { expenseDescription = it },
                onExpenseAmountChange = { expenseAmount = it },
                onDismiss = { showExpenseDialog = false },
                onConfirm = {
                    val amount = expenseAmount.toDoubleOrNull() ?: 0.0
                    val userId = getUserId() ?: return@ExpenseDialog
                    val newItem = IncomeExpenseItem(
                        userId = userId,
                        description = expenseDescription,
                        amount = amount,
                        isActive = true,
                        type = "expense"
                    )
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            database.incomeExpenseDao().insertExpenseItem(newItem)
                        }
                        homeScreenViewModel.refreshLists()
                    }
                    showExpenseDialog = false
                    expenseAmount = ""
                    expenseDescription = ""
                }
            )
        }
    }
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Income") },
        text = {
            Column {
                TextField(
                    value = incomeDescription,
                    onValueChange = onIncomeDescriptionChange,
                    label = { Text("Description") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = incomeAmount,
                    onValueChange = onIncomeAmountChange,
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirm Income")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Expense") },
        text = {
            Column {
                TextField(
                    value = expenseDescription,
                    onValueChange = onExpenseDescriptionChange,
                    label = { Text("Description") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = expenseAmount,
                    onValueChange = onExpenseAmountChange,
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirm Expense")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getUserId(): String? {
    val currentUser = FirebaseAuth.getInstance().currentUser
    return currentUser?.uid
}
