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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFFFBF2)
                ) {
                    LoginRegisterScreen(
                        onLoginSuccess = {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LoginRegisterScreen(onLoginSuccess: () -> Unit) {
    var isLogin by remember { mutableStateOf(true) }
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
                    colors = listOf(Color(0xFFFFFBF2), Color(0xFFF8F5EC))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isLogin) "Welcome Back!" else "Create Account",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF5A4A3B)
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        if (!isLogin) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFFA874FF))
        } else {
            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (!isLogin) {
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
                                    onLoginSuccess()
                                },
                                onFailure = { e ->
                                    val errorMessage = e.message ?: "An error occurred"
                                    val displayMessage = when {
                                        errorMessage.contains("Username already exists", ignoreCase = true) -> "This username already exists"
                                        errorMessage.contains("Email already exists", ignoreCase = true) -> "This email already exists"
                                        errorMessage.startsWith("Error 400:") -> {
                                            try {
                                                val jsonError = JSONObject(errorMessage.substringAfter("Error 400: "))
                                                jsonError.optString("error", errorMessage)
                                            } catch (je: Exception) {
                                                errorMessage
                                            }
                                        }
                                        else -> errorMessage
                                    }
                                    Toast.makeText(context, displayMessage, Toast.LENGTH_LONG).show()
                                    Log.e("LOGIN", "Error: $errorMessage", e)
                                }
                            )
                        } catch (e: Exception) {
                            Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA874FF))
            ) {
                Text(
                    text = if (isLogin) "LOGIN" else "REGISTER",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isLogin) "Don't have an account? Register" else "Already have an account? Login",
            color = Color(0xFF7B6A58),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { isLogin = !isLogin }
        )
    }
}
