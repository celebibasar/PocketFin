package com.basarcelebi.pocketfin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object AskToGemini : Screen("ask_to_gemini", "Ask to Gemini", Icons.Filled.Phone)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
}
