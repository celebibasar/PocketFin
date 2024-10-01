package com.basarcelebi.pocketfin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.basarcelebi.pocketfin.ui.theme.VibrantGreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        checkIfUserAlreadySignedIn()

        setContent {
            SignInScreen()
        }
    }

    private fun checkIfUserAlreadySignedIn() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    @Composable
    fun SignInScreen() {
        val context = LocalContext.current
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var emailError by remember { mutableStateOf("") }
        var passwordError by remember { mutableStateOf("") }
        val isDarkTheme = isSystemInDarkTheme()

        val signInLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {
            handleGoogleSignInResult(it, context)
        }

        MaterialTheme(
            colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme(),
        ) {
            SignInForm(
                email = email,
                onEmailChange = { email = it; emailError = "" },
                password = password,
                onPasswordChange = { password = it; passwordError = "" },
                onSignInClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        signInWithEmailPassword(email, password, context) { success, errorMessage ->
                            if (!success) {
                                passwordError = errorMessage
                            }
                        }
                    }
                },
                onGoogleSignInClick = { signInWithGoogle(signInLauncher, context) },
                onSignUpClick = { navigateToSignUp(context) },
                emailError = emailError,
                passwordError = passwordError,
                isDarkTheme = isDarkTheme
            )
        }
    }

    @Composable
    fun SignInForm(
        email: String,
        onEmailChange: (String) -> Unit,
        password: String,
        onPasswordChange: (String) -> Unit,
        onSignInClick: () -> Unit,
        onGoogleSignInClick: () -> Unit,
        onSignUpClick: () -> Unit,
        emailError: String,
        passwordError: String,
        isDarkTheme: Boolean
    ) {
        val textColor = if (isDarkTheme) Color.White else Color.Black

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sign In",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                fontSize = 28.sp,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            InputField(
                value = email,
                onValueChange = onEmailChange,
                label = "Email",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                textColor = textColor
            )

            InputField(
                value = password,
                onValueChange = onPasswordChange,
                label = "Password",
                leadingIcon = Icons.Default.Lock,
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
                textColor = textColor,
                isError = passwordError.isNotEmpty()
            )

            if (passwordError.isNotEmpty()) {
                Text(
                    text = passwordError,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }

            SignInButton(onSignInClick, textColor)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            GoogleSignInButton(onClick = onGoogleSignInClick, textColor = textColor)

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onSignUpClick) {
                Text("Don't have an account? Sign Up", color = textColor,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                    fontSize = 18.sp)
            }
        }
    }

    @Composable
    fun InputField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
        keyboardType: KeyboardType,
        textColor: Color,
        visualTransformation: VisualTransformation = VisualTransformation.None,
        isError: Boolean = false
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = textColor) },
            singleLine = true,
            visualTransformation = visualTransformation,
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = textColor) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            colors = TextFieldDefaults.textFieldColors(
                textColor = textColor,
                focusedIndicatorColor = textColor,
                unfocusedIndicatorColor = textColor,
                cursorColor = textColor
            ),
            isError = isError
        )
    }

    @Composable
    fun SignInButton(onClick: () -> Unit, textColor: Color) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = androidx.compose.material.ButtonDefaults.buttonColors(
                backgroundColor = VibrantGreen,
                contentColor = textColor
            ),
            shape = RoundedCornerShape(40.dp)
        ) {
            Text(
                text = "Sign In",
                color = textColor,
                fontSize = 18.sp
            )
        }
    }

    private fun signInWithEmailPassword(email: String, password: String, context: Context, callback: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(context, "Signed in as ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                    callback(true, "")
                } else {
                    callback(false, "Wrong email or password, please try again!")
                }
            }
    }

    private fun signInWithGoogle(signInLauncher: androidx.activity.result.ActivityResultLauncher<Intent>, context: Context) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun handleGoogleSignInResult(result: ActivityResult, context: Context) {
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(context, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Signed in as ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToSignUp(context: Context) {
        context.startActivity(Intent(context, SignUpActivity::class.java))
    }
    @Composable
    fun GoogleSignInButton(onClick: () -> Unit, textColor: Color) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(40.dp)
                ),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.White,
                contentColor = textColor
            ),
            shape = RoundedCornerShape(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = "Google Sign In",
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sign In with Google",
                color = textColor,
                fontSize = 18.sp
            )
        }
    }

}

@Preview
@Composable
fun SignInScreenPreview() {
    SignInActivity().SignInScreen()
}
