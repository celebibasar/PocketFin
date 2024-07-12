package com.basarcelebi.pocketfin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
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
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Logo()
                    }
                },
                backgroundColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        },
        backgroundColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavigationHost(navController, Modifier.padding(innerPadding), database, scope)
    }
}

@Composable
fun Logo() {
    val logo: Painter = painterResource(id = R.drawable.pocket_fin_logo)
    val openUrlLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    Image(
        painter = logo,
        contentDescription = "Logo",
        modifier = Modifier.size(132.dp)
            .clickable {
                val url = "https://github.com/celebibasar/PocketFin"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                openUrlLauncher.launch(intent)
            }
    )
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
                        "home" -> Icon(Icons.Default.Home, contentDescription = "Home", tint = VibrantGreen)
                        "askToGemini" -> Icon(Icons.Default.Settings, contentDescription = "AskToGemini", tint = VibrantGreen)
                        "profile" -> Icon(Icons.Default.Person, contentDescription = "Profile", tint = VibrantGreen)
                        else -> Icon(Icons.Default.Home, contentDescription = "Home", tint = VibrantGreen)
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
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black


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
                modifier = Modifier.padding(vertical = 8.dp),
                color = textColor
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
                        val amount = incomeAmount.replace(",", ".").replace("-","").replace("+","").toDoubleOrNull() ?: 0.0
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
                        val amount = expenseAmount.replace(",", ".").replace("-","").replace("+","").toDoubleOrNull() ?: 0.0
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
            colors = CheckboxDefaults.colors(checkedColor = checkboxColor)
        )
        Column {

            Text(text = item.description ?: "", style = MaterialTheme.typography.bodyLarge, color = textColor)
            Text(
                text = "€${String.format("%.2f", item.amount ?: 0.0)}",
                style = MaterialTheme.typography.bodyMedium,
                color = itemColor
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { isDropdownExpanded = !isDropdownExpanded }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
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
    var description by remember { mutableStateOf(item.description ?: "") }
    var amount by remember { mutableStateOf(item.amount?.toString() ?: "") }
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val itemTypeColor = if (item.type == "income") VibrantGreen else Red700

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Item",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = textColor) },
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
                    )
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val updatedItem = item.copy(description = description, amount = amount.replace(",", ".").replace("-","").replace("+","").toDoubleOrNull() ?: 0.0)
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
                color = textColor
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
                    )
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
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black
    AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Add Expense",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = textColor
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
                    )
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
