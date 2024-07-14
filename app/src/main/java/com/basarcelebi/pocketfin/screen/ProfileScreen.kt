package com.basarcelebi.pocketfin.screen

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.basarcelebi.pocketfin.R
import com.basarcelebi.pocketfin.SignInActivity
import com.basarcelebi.pocketfin.data.ProfileData
import com.basarcelebi.pocketfin.database.User
import com.basarcelebi.pocketfin.network.UserAuth
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    auth: UserAuth = UserAuth(),
    navController: NavController = rememberNavController()
) {
    val user = auth.user
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black

    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme(),
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {

            Spacer(modifier = Modifier.height(15.dp))

            Card(
                modifier = Modifier.fillMaxWidth()
                    .border(1.dp, if (isDarkTheme) Color.White else Color.DarkGray, RoundedCornerShape(30.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface

                ),
                shape = RoundedCornerShape(30.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    user?.let {
                        Text(text = "${it.displayName}", fontSize = 20.sp, color = textColor, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "${it.email}", fontSize = 16.sp,color = textColor, style = MaterialTheme.typography.bodyMedium.copy(fontStyle = Italic))
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            val lazyItems = ProfileData.ProfileOptionsData()
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(lazyItems.size) { index ->
                    val item = lazyItems[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(240.dp))
                            .clickable {
                                when (index) {
                                    0 -> navController.navigate("account")
                                    1 -> navController.navigate("security")
                                    2 -> navController.navigate("about")
                                    3 -> {
                                        coroutineScope.launch {
                                            auth.logout()
                                            Intent(context, SignInActivity::class.java).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                context.startActivity(this)
                                            }
                                        }
                                    }
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface

                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp)
                                .background(MaterialTheme.colorScheme.surface),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                modifier = Modifier.size(42.dp),
                                tint = if (isDarkTheme) Color.White else Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = item.name, fontSize = 20.sp,color = textColor, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
                                Text(text = item.description, fontSize = 12.sp, color = if (isDarkTheme) Color.White else Color.Black, style = MaterialTheme.typography.bodyMedium.copy(fontStyle = Italic))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    val navController = rememberNavController()

   //  ProfileScreen(navController = navController)
}
