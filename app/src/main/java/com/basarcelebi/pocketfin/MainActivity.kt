package com.basarcelebi.pocketfin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.basarcelebi.pocketfin.database.PocketFinDatabase
import com.basarcelebi.pocketfin.network.UserAuth
import com.basarcelebi.pocketfin.screen.AboutScreen
import com.basarcelebi.pocketfin.screen.AccountScreen
import com.basarcelebi.pocketfin.screen.HomeScreen
import com.basarcelebi.pocketfin.screen.PrivacyScreen
import com.basarcelebi.pocketfin.screen.ProfileScreen
import com.basarcelebi.pocketfin.ui.theme.PocketFinTheme
import com.basarcelebi.pocketfin.ui.theme.VibrantGreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var database: PocketFinDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase Authentication instance
        auth = FirebaseAuth.getInstance()

        // Check if the user is signed in
        val currentUser = auth.currentUser

        // If the user is signed in, navigate to Home; otherwise, go to SignInActivity
        if (currentUser != null) {
            database = PocketFinDatabase.getDatabase(applicationContext)
            setContent {
                PocketFinTheme {
                    PocketFinApp(database = database)
                }
            }
        } else {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

@Composable
fun PocketFinApp(
    navController: NavHostController = rememberNavController(),
    database: PocketFinDatabase,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val isBackStackEmpty by remember(navBackStackEntry) {
        mutableStateOf(navBackStackEntry?.destination?.route == "home")
    }

    Scaffold(
        topBar = { PocketFinAppTopAppBar(navController, isBackStackEmpty) },
        bottomBar = { BottomNavigationBar(navController) },
        backgroundColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavigationHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            database = database,
            scope = scope
        )
    }
}

@Composable
fun PocketFinAppTopAppBar(navController: NavController, isBackStackEmpty: Boolean) {
    val title = @Composable {
        Box(
            Modifier.padding(start = 70.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Logo()
        }
    }

    val navigationIcon = @Composable {
        if (!isBackStackEmpty) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }

    TopAppBar(
        title = title,
        navigationIcon = navigationIcon,
        backgroundColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun Logo() {
    val isDarkTheme = isSystemInDarkTheme()
    val logo: Painter = painterResource(
        id = if (isDarkTheme) R.drawable.pocket_fin_logo_darktheme else R.drawable.pocket_fin_logo
    )

    val openUrlLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    Image(
        painter = logo,
        contentDescription = "Logo",
        modifier = Modifier
            .size(132.dp)
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
    // Get the UserDao from the database
    val userDao = database.userDao()

    NavHost(navController, startDestination = Screen.Home.route, modifier = modifier) {
        composable(Screen.Home.route) {
            HomeScreen(database, scope)
        }
        composable(Screen.AskToGemini.route) {
            ChatActivity().ChatScreen(paddingValues = PaddingValues(16.dp))
        }
        composable(Screen.Profile.route) {
            ProfileScreen(UserAuth(), navController, userDao) // Pass userDao to ProfileScreen
        }
        composable(Screen.Account.route) {
            AccountScreen()
        }
        composable(Screen.Security.route) {
            PrivacyScreen()
        }
        composable(Screen.About.route) {
            AboutScreen()
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavHostController?) {
    val currentRoute = navController?.currentDestination?.route

    BottomNavigation(
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        val items = listOf("home", "gemini", "profile")
        items.forEach { screen ->
            BottomNavigationItem(
                icon = {
                    when (screen) {
                        "home" -> Icon(
                            Icons.Default.Home,
                            contentDescription = "Home",
                            tint = VibrantGreen
                        )
                        "gemini" -> Icon(
                            painter = painterResource(id = R.drawable.gemini),
                            contentDescription = "Gemini",
                            tint = VibrantGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        "profile" -> Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = VibrantGreen
                        )
                        else -> Icon(
                            Icons.Default.Home,
                            contentDescription = "Home",
                            tint = VibrantGreen
                        )
                    }
                },
                label = {
                    Text(
                        text = screen.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (currentRoute == screen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                selected = currentRoute == screen,
                onClick = {
                    navController?.navigate(screen) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                alwaysShowLabel = false
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewPocketFinAppTopAppBar() {
    PocketFinAppTopAppBar(navController = rememberNavController(), isBackStackEmpty = true)
}

@Preview(showBackground = true)
@Composable
fun PreviewLogo() {
    Logo()
}

@Preview(showBackground = true)
@Composable
fun PreviewBottomNavigationBar() {
    BottomNavigationBar(navController = rememberNavController())
}

