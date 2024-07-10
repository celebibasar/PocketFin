package com.basarcelebi.pocketfin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
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
import com.basarcelebi.pocketfin.ui.theme.PocketFinTheme
import com.basarcelebi.pocketfin.viewmodel.HomeScreenViewModel
import kotlinx.coroutines.CoroutineScope
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
            // Ekran içeriğini buraya yerleştir
        }
        composable(Screen.Profile.route) {
            // Ekran içeriğini buraya yerleştir
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    BottomNavigation {
        val items = listOf("home", "askToGemini", "profile")
        items.forEach { screen ->
            BottomNavigationItem(
                icon = { Icon(Icons.Default.Home, contentDescription = null) },
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

    // Gelir ve gider listelerini Room'dan LiveData olarak al
    val incomeList by homeScreenViewModel.incomeList.observeAsState(emptyList())
    val expenseList by homeScreenViewModel.expenseList.observeAsState(emptyList())
    val totalAmountLiveData by homeScreenViewModel.totalAmount.observeAsState(0.0)

    // Güncellenen totalAmount değerini kullan
    LaunchedEffect(totalAmountLiveData) {
        totalAmount = totalAmountLiveData
    }

    // Veritabanından verileri almak için başlangıçta çalışan etki
    LaunchedEffect(Unit) {
        homeScreenViewModel.refreshLists()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gelirler bölümü
        Text("Gelirler")
        incomeList.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = item.isActive,
                    onCheckedChange = {
                        item.isActive = it
                        homeScreenViewModel.refreshLists()
                    }
                )
                Text("${item.description}: €${String.format("%.2f", item.amount)}")
            }
        }
        Button(onClick = { showIncomeDialog = true }) {
            Text("Gelir Ekle")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Giderler bölümü
        Text("Giderler")
        expenseList.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = item.isActive,
                    onCheckedChange = {
                        item.isActive = it
                        homeScreenViewModel.refreshLists()
                    }
                )
                Text("${item.description}: -€${String.format("%.2f", item.amount)}")
            }
        }
        Button(onClick = { showExpenseDialog = true }) {
            Text("Gider Ekle")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Genel Bakış bölümü
        Text("Genel Bakış", style = MaterialTheme.typography.h6)
        Text("€${String.format("%.2f", totalAmount)}", style = MaterialTheme.typography.h4)

        // Dialoglar
        if (showIncomeDialog) {
            IncomeDialog(
                incomeDescription = incomeDescription,
                incomeAmount = incomeAmount,
                onIncomeDescriptionChange = { incomeDescription = it },
                onIncomeAmountChange = { incomeAmount = it },
                onDismiss = { showIncomeDialog = false },
                onConfirm = {
                    val amount = incomeAmount.toDoubleOrNull() ?: 0.0
                    val newItem = IncomeExpenseItem(description = incomeDescription, amount = amount, isActive = true, type = "income")
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
                    val newItem = IncomeExpenseItem(description = expenseDescription, amount = amount, isActive = true, type = "expense")
                    scope.launch {
                        withContext(scope.coroutineContext) {
                            database.incomeExpenseDao().insertExpenseItem(newItem)
                            homeScreenViewModel.refreshLists()

                        }
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
        title = { Text("Yeni Gelir") },
        text = {
            Column {
                TextField(
                    value = incomeDescription,
                    onValueChange = onIncomeDescriptionChange,
                    label = { Text("İsim") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = incomeAmount,
                    onValueChange = onIncomeAmountChange,
                    label = { Text("Miktar") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Geliri Onayla")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("İptal")
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
        title = { Text("Yeni Gider") },
        text = {
            Column {
                TextField(
                    value = expenseDescription,
                    onValueChange = onExpenseDescriptionChange,
                    label = { Text("İsim") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = expenseAmount,
                    onValueChange = onExpenseAmountChange,
                    label = { Text("Miktar") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Gideri Onayla")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

