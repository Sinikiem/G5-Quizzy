package com.example.quizzy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizzy.network.NetworkClient
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Constants for UI styling in Login screen to avoid magic numbers.
 */
private object LoginUI {
    val BackgroundLight = Color(0xFFFFFBF2)
    val BackgroundGradientEnd = Color(0xFFF8F5EC)
    val TextPrimary = Color(0xFF5A4A3B)
    val TextSecondary = Color(0xFF7B6A58)
    val PrimaryAction = Color(0xFFA874FF)
    
    val CornerRadiusTextField = 16.dp
    val CornerRadiusButton = 28.dp
    val PaddingScreen = 24.dp
    val SpacingMedium = 16.dp
    val SpacingLarge = 32.dp
    val ButtonHeight = 56.dp
}

/**
 * Activity that handles user authentication, providing both login and registration interfaces.
 * Automatically redirects to [MainActivity] if a session is already active.
 */
class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LoginUI.BackgroundLight
                ) {
                    LoginRegisterScreen(
                        onLoginSuccess = { navigateToMain() }
                    )
                }
            }
        }
    }

    /**
     * Helper to navigate to main screen and finish login activity.
     */
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("start_screen", "Home")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

/**
 * A combined screen for Login and Registration.
 * 
 * @param onLoginSuccess Callback invoked when the user successfully authenticates.
 */
@Composable
fun LoginRegisterScreen(onLoginSuccess: () -> Unit) {
    var isLoginMode by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(LoginUI.BackgroundLight, LoginUI.BackgroundGradientEnd)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(LoginUI.PaddingScreen),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isLoginMode) "Welcome Back!" else "Create Account",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LoginUI.TextPrimary
        )

        Spacer(modifier = Modifier.height(LoginUI.SpacingLarge))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(LoginUI.CornerRadiusTextField),
            singleLine = true
        )

        if (!isLoginMode) {
            Spacer(modifier = Modifier.height(LoginUI.SpacingMedium))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(LoginUI.CornerRadiusTextField),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(LoginUI.SpacingMedium))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(LoginUI.CornerRadiusTextField),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(LoginUI.SpacingLarge))

        if (isLoading) {
            CircularProgressIndicator(color = LoginUI.PrimaryAction)
        } else {
            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (!isLoginMode) {
                        if (email.isBlank()) {
                            Toast.makeText(context, "Email is required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                    }
                    scope.launch {
                        isLoading = true
                        handleAuthentication(
                            isLogin = isLoginMode,
                            username = username,
                            password = password,
                            email = email,
                            sessionManager = sessionManager,
                            onSuccess = onLoginSuccess,
                            onFailure = { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            },
                            onFinally = { isLoading = false }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LoginUI.ButtonHeight),
                shape = RoundedCornerShape(LoginUI.CornerRadiusButton),
                colors = ButtonDefaults.buttonColors(containerColor = LoginUI.PrimaryAction)
            ) {
                Text(
                    text = if (isLoginMode) "LOGIN" else "REGISTER",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isLoginMode) "Don't have an account? Register" else "Already have an account? Login",
            color = LoginUI.TextSecondary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { isLoginMode = !isLoginMode }
        )
    }
}

/**
 * Handles the network call for login or registration.
 * 
 * Algorithm:
 * 1. Build JSON payload based on auth mode.
 * 2. Send POST request to appropriate endpoint.
 * 3. On success: Parse user ID and name, then save to session.
 * 4. On failure: Map raw error messages to user-friendly strings.
 */
private suspend fun handleAuthentication(
    isLogin: Boolean,
    username: String,
    password: String,
    email: String,
    sessionManager: SessionManager,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit,
    onFinally: () -> Unit
) {
    try {
        val endpoint = if (isLogin) "/users/login" else "/users/register"
        val body = JSONObject().apply {
            put("username", username)
            put("password", password)
            if (!isLogin) {
                put("email", email)
                put("role", "STUDENT")
            }
        }

        val result = NetworkClient.post(endpoint, body)
        result.fold(
            onSuccess = { json ->
                val id = json.optLong("userId", -1L).takeIf { it != -1L } ?: json.optLong("id", -1L)
                val user = json.optString("username", username)
                sessionManager.saveUser(id, user)
                onSuccess()
            },
            onFailure = { e ->
                val errorMessage = e.message ?: "An error occurred"
                onFailure(mapErrorMessage(errorMessage))
                Log.e("LOGIN", "Auth Error: $errorMessage", e)
            }
        )
    } catch (e: Exception) {
        onFailure("An error occurred")
    } finally {
        onFinally()
    }
}

/**
 * Maps technical error messages from the backend to localized, human-readable strings.
 */
private fun mapErrorMessage(errorMessage: String): String {
    return when {
        errorMessage.contains("account doesn't exist", ignoreCase = true) -> "This account doesn't exist"
        errorMessage.contains("username already exists", ignoreCase = true) -> "This username already exists"
        errorMessage.contains("email already exists", ignoreCase = true) -> "This email already exists"
        errorMessage.contains("Incorrect password", ignoreCase = true) -> "Incorrect password"
        errorMessage.startsWith("Error 401:") -> parseJsonError(errorMessage, "Error 401: ", "This account doesn't exist")
        errorMessage.startsWith("Error 400:") -> parseJsonError(errorMessage, "Error 400: ", errorMessage)
        else -> errorMessage
    }
}

private fun parseJsonError(message: String, prefix: String, default: String): String {
    return try {
        val jsonError = JSONObject(message.substringAfter(prefix))
        jsonError.optString("error", default)
    } catch (je: Exception) {
        default
    }
}
