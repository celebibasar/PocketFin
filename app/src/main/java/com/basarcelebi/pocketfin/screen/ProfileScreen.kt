package com.basarcelebi.pocketfin.screen

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.basarcelebi.pocketfin.R
import com.basarcelebi.pocketfin.SignInActivity
import com.basarcelebi.pocketfin.data.ProfileData
import com.basarcelebi.pocketfin.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    navController: NavController = rememberNavController()
) {
    val user = viewModel.user
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
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface

                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    val rainbowColorsBrush = remember {
                        Brush.sweepGradient(
                            listOf(
                                Color(0xFF9575CD),
                                Color(0xFFBA68C8),
                                Color(0xFFE57373),
                                Color(0xFFFFB74D),
                                Color(0xFFFFF176),
                                Color(0xFFAED581),
                                Color(0xFF4DD0E1),
                                Color(0xFF9575CD)
                            )
                        )
                    }
                    val borderWidth = 4.dp
                    Image(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(75.dp)
                            .border(BorderStroke(borderWidth, rainbowColorsBrush), CircleShape)
                            .clickable { }
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(top = 3.dp, bottom = 8.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    user?.let {
                        Text(text = "${it.displayName}", fontSize = 20.sp, color = textColor)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "${it.email}", fontSize = 16.sp,color = textColor)
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
                            .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                            .clickable {
                                when (index) {
                                    0 -> navController.navigate("account")
                                    1 -> navController.navigate("security")
                                    2 -> navController.navigate("about")
                                    3 -> {
                                        coroutineScope.launch {
                                            viewModel.logout()
                                            Intent(context, SignInActivity::class.java).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                context.startActivity(this)
                                            }
                                        }
                                    }
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface

                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = item.name, fontSize = 18.sp,color = textColor)
                                Text(text = item.description, fontSize = 14.sp, color = textColor)
                            }
                        }
                    }
                }
            }
        }
    }
}
