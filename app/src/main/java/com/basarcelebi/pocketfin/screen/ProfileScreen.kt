package com.basarcelebi.pocketfin.screen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil.compose.rememberImagePainter
import com.basarcelebi.pocketfin.R
import com.basarcelebi.pocketfin.SignInActivity
import com.basarcelebi.pocketfin.database.User
import com.basarcelebi.pocketfin.database.UserDao
import com.basarcelebi.pocketfin.network.UserAuth
import com.basarcelebi.pocketfin.network.UserAuth.Companion.logout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(auth: UserAuth = UserAuth(), navController: NavHostController, userDao: UserDao) {
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
            // ProfileCard fonksiyonuna userDao parametresini ekleyin
            ProfileCard(auth = auth, userDao = userDao)
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
fun ProfileCard(auth: UserAuth, userDao: UserDao) {
    val poppins = FontFamily(Font(R.font.poppins))
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val user = auth.user

    var profileImageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            profileImageUri = uri
            user?.let { currentUser ->
                saveImageUriToDatabase(
                    currentUser.id,
                    uri.toString(),
                    userDao
                )
            }
        }
    }

    // Fetch the profile image URI when the user object changes
    LaunchedEffect(user) {
        user?.let {
            profileImageUri = it.profileImageUrl?.let { uriString ->
                Uri.parse(uriString).also { uri ->
                    Log.d("ProfileCard", "Fetched profile image URI: $uri")
                }
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        profileImageUri?.let {
            Image(
                painter = rememberImagePainter(it),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape) // Circular profile image
            )
        } ?: run {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Gray) // Placeholder background
            )
        }

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
    }
}

fun saveImageUriToDatabase(userId: String, imageUri: String, userDao: UserDao) {
    // Profil resmini veritabanına kaydetmek için Room üzerinden güncelleme yapalım
    CoroutineScope(Dispatchers.IO).launch {
        userDao.updateProfileImage(userId, imageUri)
    }
}



@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    val navController = rememberNavController()
    val userDao = MockUserDao() // Use the mock implementation here
    ProfileScreen(navController = navController, userDao = userDao)
}

class MockUserDao : UserDao {
    override suspend fun insert(user: User) {
        // Mock implementation
    }

    override suspend fun updateUser(user: User) {
        // Mock implementation
    }

    override suspend fun getUser(userId: String): User? {
        // Return a mock user for preview
        return User(id = "1", userName = "John Doe", profileImageUrl = "http://example.com/image.jpg")
    }

    override suspend fun updateProfileImage(userId: String, profileImageUrl: String) {
        // Mock implementation
    }
}


