package com.basarcelebi.pocketfin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object AskToGemini : Screen("askToGemini", "Ask to Gemini", Icons.Filled.Warning)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
    object Account : Screen("account", "Account", null)
    object Security : Screen("security", "Security", null)
    object About : Screen("about", "About", null)
}
