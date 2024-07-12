package com.basarcelebi.pocketfin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
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
import com.basarcelebi.pocketfin.ui.theme.Red500
import com.basarcelebi.pocketfin.ui.theme.Red700
import com.basarcelebi.pocketfin.ui.theme.VibrantGreen
import com.basarcelebi.pocketfin.ui.theme.VibrantPink
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
        },
        backgroundColor = Color.LightGray,
        modifier = Modifier.fillMaxSize()
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
    val currentRoute = navController.currentDestination?.route

    BottomNavigation(
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        val items = listOf("home", "askToGemini", "profile")
        items.forEach { screen ->
            BottomNavigationItem(
                icon = {
                    when (screen) {
                        "home" -> Icon(Icons.Default.Home, contentDescription = "Home")
                        "askToGemini" -> Icon(Icons.Default.Settings, contentDescription = "AskToGemini")
                        "profile" -> Icon(Icons.Default.Person, contentDescription = "Profile")
                        else -> Icon(Icons.Default.Home, contentDescription = "Home")
                    }
                },
                label = {
                    Text(
                        text = screen.capitalize(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (currentRoute == screen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                selected = currentRoute == screen,
                onClick = {
                    navController.navigate(screen) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                alwaysShowLabel = false
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

    val incomeList by homeScreenViewModel.incomeList.observeAsState(emptyList())
    val expenseList by homeScreenViewModel.expenseList.observeAsState(emptyList())

    LaunchedEffect(incomeList, expenseList) {
        val activeIncomeAmount = incomeList.filter { it.isActive }.sumByDouble { it.amount ?: 0.0 }
        val activeExpenseAmount = expenseList.filter { it.isActive }.sumByDouble { it.amount ?: 0.0 }
        totalAmount = activeIncomeAmount - activeExpenseAmount
    }

    LaunchedEffect(Unit) {
        homeScreenViewModel.refreshLists()
    }

    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                onClick = { showIncomeDialog = true },
                modifier = Modifier
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = VibrantGreen,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Add Income", color = MaterialTheme.colorScheme.onSecondary, style = TextStyle(fontWeight = FontWeight.Bold))
            }


            Spacer(modifier = Modifier.height(16.dp))
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
                onClick = { showExpenseDialog = true },
                modifier = Modifier
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Red500,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Add Expense", color = MaterialTheme.colorScheme.onSecondary, style = TextStyle(fontWeight = FontWeight.Bold))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Total Overview",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "€${String.format("%.2f", totalAmount)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (totalAmount >= 0) VibrantGreen else Red500
            )


            if (showIncomeDialog) {
                IncomeDialog(
                    incomeDescription = incomeDescription,
                    incomeAmount = incomeAmount,
                    onIncomeDescriptionChange = { incomeDescription = it },
                    onIncomeAmountChange = { incomeAmount = it },
                    onDismiss = { showIncomeDialog = false },
                    onConfirm = {
                        val amount = incomeAmount.replace(",", ".").toDoubleOrNull() ?: 0.0
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
                        val amount = expenseAmount.replace(",", ".").toDoubleOrNull() ?: 0.0
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
}


@Composable
private fun IncomeExpenseItemRow(
    item: IncomeExpenseItem,
    scope: CoroutineScope,
    database: PocketFinDatabase
) {
    val homeScreenViewModel: HomeScreenViewModel = viewModel()
    val itemColor = if (item.type == "income") VibrantGreen else VibrantPink
    val checkboxColor = if (item.type == "income") VibrantGreen else VibrantPink

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
            colors = CheckboxDefaults.colors(checkedColor = checkboxColor)
        )
        Column {
            Text(text = item.description ?: "", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "€${String.format("%.2f", item.amount ?: 0.0)}",
                style = MaterialTheme.typography.bodyMedium,
                color = itemColor
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
        title = {
            Text(
                "Add Income",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = VibrantGreen
            )
        },
        text = {
            Column {
                TextField(
                    value = incomeDescription,
                    onValueChange = onIncomeDescriptionChange,
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = incomeAmount,
                    onValueChange = onIncomeAmountChange,
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Expense",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Red500
            )
        },
        text = {
            Column {
                TextField(
                    value = expenseDescription,
                    onValueChange = onExpenseDescriptionChange,
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = expenseAmount,
                    onValueChange = onExpenseAmountChange,
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
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


fun getUserId(): String? {
    val currentUser = FirebaseAuth.getInstance().currentUser
    return currentUser?.uid
}
