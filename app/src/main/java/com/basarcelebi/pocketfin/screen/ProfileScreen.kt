package com.basarcelebi.pocketfin.screen

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.basarcelebi.pocketfin.R
import com.basarcelebi.pocketfin.SignInActivity
import com.basarcelebi.pocketfin.network.UserAuth
import com.basarcelebi.pocketfin.network.UserAuth.Companion.logout

@Composable
fun ProfileScreen(auth: UserAuth = UserAuth(), navController: NavHostController) {
    val poppins = FontFamily(Font(R.font.poppins))
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        item {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = poppins,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                color = textColor
            )
        }

        item {
            ProfileCard(auth)
        }

        item {
            Text(
                text = "Manage your account",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                color = textColor
            )
        }

        item {
            SettingsBox(navController)
            SecurityBox(navController)
            AboutBox(navController)
            LogOutBox(auth)
        }
    }
}

@Composable
fun SettingsBox(navController: NavHostController) {
    BoxItem("Settings", R.drawable.ic_settings) {
        navController.navigate("settings")
    }
}

@Composable
fun SecurityBox(navController: NavHostController) {
    BoxItem("Security", R.drawable.ic_security) {
        navController.navigate("security")
    }
}

@Composable
fun AboutBox(navController: NavHostController) {
    BoxItem("About", R.drawable.ic_about) {
        navController.navigate("about")
    }
}

@Composable
fun LogOutBox(auth: UserAuth) {
    val context = LocalContext.current // Get context here

    BoxItem("Log Out", R.drawable.ic_logout) {
        logout() // Call the logout function
        val intent = Intent(context, SignInActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear task
        }
        context.startActivity(intent)
        (context as? Activity)?.finish() // Optional: Finish the current activity
    }
}

@Composable
fun BoxItem(text: String, iconRes: Int, onClick: () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }, // Invoke onClick directly
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                modifier = Modifier.size(24.dp),
                tint = textColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )
        }
    }
}

@Composable
fun ProfileCard(auth: UserAuth) {
    val poppins = FontFamily(Font(R.font.poppins))
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val user = auth.user

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(
            modifier = Modifier
                .padding(start = 16.dp, bottom = 16.dp, end = 16.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            user?.let {
                Text(
                    text = it.displayName ?: "Unknown User",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold
                    ),
                    color = textColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it.email ?: "No Email",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold
                    ),
                    fontSize = 16.sp,
                    color = textColor
                )
            }
        }

        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "Profile",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    val navController = rememberNavController()
    ProfileScreen(navController = navController)
}
