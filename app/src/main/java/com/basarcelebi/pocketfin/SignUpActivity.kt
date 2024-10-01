package com.basarcelebi.pocketfin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.basarcelebi.pocketfin.ui.theme.VibrantGreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            SignUpScreen()
        }
    }

    @Composable
    fun SignUpScreen() {
        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var repassword by remember { mutableStateOf("") }

        var firstNameError by remember { mutableStateOf("") }
        var lastNameError by remember { mutableStateOf("") }
        var emailError by remember { mutableStateOf("") }
        var passwordError by remember { mutableStateOf("") }
        var repasswordError by remember { mutableStateOf("") }

        val isDarkTheme = isSystemInDarkTheme()
        val textColor = if (isDarkTheme) Color.White else Color.Black

        MaterialTheme(
            colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme(),
            typography = MaterialTheme.typography,
            shapes = MaterialTheme.shapes
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    fontSize = 28.sp,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // First Name TextField
                SignUpTextField(
                    value = firstName,
                    onValueChange = { firstName = it; firstNameError = "" },
                    label = "First Name",
                    textColor = textColor,
                    leadingIcon = Icons.Default.Person,
                    errorMessage = firstNameError
                )

                // Last Name TextField
                SignUpTextField(
                    value = lastName,
                    onValueChange = { lastName = it; lastNameError = "" },
                    label = "Last Name",
                    textColor = textColor,
                    leadingIcon = Icons.Default.Person,
                    errorMessage = lastNameError
                )

                // Email TextField
                SignUpTextField(
                    value = email,
                    onValueChange = { email = it; emailError = "" },
                    label = "Email",
                    textColor = textColor,
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    errorMessage = emailError
                )

                // Password TextField
                SignUpTextField(
                    value = password,
                    onValueChange = { password = it; passwordError = "" },
                    label = "Password",
                    textColor = textColor,
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardType = KeyboardType.Password,
                    errorMessage = passwordError
                )

                // Repeat Password TextField
                SignUpTextField(
                    value = repassword,
                    onValueChange = { repassword = it; repasswordError = "" },
                    label = "Repeat Password",
                    textColor = textColor,
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardType = KeyboardType.Password,
                    errorMessage = repasswordError
                )

                // Sign Up Button
                Button(
                    onClick = {
                        signUpWithEmailPassword(
                            firstName, lastName, email, password, repassword
                        ) { success, errors ->
                            if (!success) {
                                firstNameError = errors["firstName"] ?: ""
                                lastNameError = errors["lastName"] ?: ""
                                emailError = errors["email"] ?: ""
                                passwordError = errors["password"] ?: ""
                                repasswordError = errors["repassword"] ?: ""
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VibrantGreen,
                        contentColor = textColor
                    ),
                    shape = RoundedCornerShape(40.dp)
                ) {
                    Text(
                        text = "Sign Up",
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        fontSize = 18.sp
                    )
                }
            }
        }
    }

    @Composable
    fun SignUpTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        textColor: Color,
        leadingIcon: ImageVector,
        visualTransformation: VisualTransformation = VisualTransformation.None,
        keyboardType: KeyboardType = KeyboardType.Text,
        errorMessage: String
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = textColor) },
            singleLine = true,
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = textColor) },
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = TextFieldDefaults.textFieldColors(
                textColor = textColor,
                backgroundColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = textColor,
                unfocusedLabelColor = textColor,
                cursorColor = textColor
            ),
            isError = errorMessage.isNotEmpty()
        )
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(8.dp)
            )
        }
    }

    private fun signUpWithEmailPassword(
        firstName: String, lastName: String, email: String,
        password: String, repassword: String, callback: (Boolean, Map<String, String>) -> Unit
    ) {
        val errors = mutableMapOf<String, String>()

        // Error Handling
        if (firstName.isBlank()) errors["firstName"] = "First name cannot be empty"
        if (lastName.isBlank()) errors["lastName"] = "Last name cannot be empty"
        if (email.isBlank()) errors["email"] = "Email cannot be empty"
        if (password.isBlank()) errors["password"] = "Password cannot be empty"
        if (repassword.isBlank()) errors["repassword"] = "Repeat password cannot be empty"
        if (password != repassword) errors["repassword"] = "Passwords do not match"

        if (errors.isNotEmpty()) {
            callback(false, errors)
            return
        }

        // Firebase Sign Up
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName("$firstName $lastName")
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileUpdateTask ->
                            if (profileUpdateTask.isSuccessful) {
                                Toast.makeText(this, "Signed up as ${user.displayName}", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, "Profile update failed: ${profileUpdateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

@Preview
@Composable
fun SignUpScreenPreview() {
    SignUpActivity().SignUpScreen()
}
