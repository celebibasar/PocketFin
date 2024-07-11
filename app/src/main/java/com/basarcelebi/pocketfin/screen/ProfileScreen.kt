package com.basarcelebi.pocketfin.screen

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.basarcelebi.pocketfin.SignInActivity
import com.basarcelebi.pocketfin.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel()
) {
    val user = viewModel.user
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        user?.let {
            Text(text = "Name: ${it.displayName}", fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Email: ${it.email}", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            coroutineScope.launch {
                viewModel.logout()
                // FirebaseAuth'dan çıkış yapıldığında SignInActivity'e geçiş yap
                Intent(context, SignInActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(this)
                }
            }
        }) {
            Text(text = "Logout")
        }
    }
}
