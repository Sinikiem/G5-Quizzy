package com.example.quizzy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()

        setContent {
            val initialScreen = intent.getStringExtra("start_screen") ?: "Home"
            var currentScreen by remember { mutableStateOf(initialScreen) }

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFFFBF2)
                ) {
                    Scaffold(
                        bottomBar = {
                            FancyNavigationBar(
                                currentScreen = currentScreen,
                                onTabSelected = { currentScreen = it }
                            )
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (currentScreen) {
                                "Home", "Dashboard" -> DashboardScreen(
                                    onStartQuiz = { currentScreen = "QuizSelection" }
                                )

                                "QuizSelection" -> QuizSelectionScreen(
                                    onGradeSelected = { gradeLevel, gradeName ->
                                        val intent = Intent(this@MainActivity, InstructionsActivity::class.java).apply {
                                            putExtra("GRADE_LEVEL", gradeLevel)
                                            putExtra("GRADE_NAME", gradeName)
                                        }
                                        startActivity(intent)
                                    }
                                )

                                "Achievements" -> {
                                    LaunchedEffect(Unit) {
                                        startActivity(Intent(this@MainActivity, AchievementsActivity::class.java))
                                        currentScreen = "Home"
                                    }
                                }

                                "Guardian" -> PlaceholderScreen("Guardian Dashboard")
                                "Settings" -> SettingsScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(onStartQuiz: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFBF2), Color(0xFFF8F5EC))
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Fancier Quizzy Logo with App Colors
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Q",
                fontSize = 80.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFA874FF) // Purple from Grade 3
            )
            Text(
                text = "uizzy",
                fontSize = 64.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF5A4A3B), // Dark Brown
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Ready to test your knowledge?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF7B6A58),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(80.dp))

        // Pulsing Play Button - Rounded Rectangle
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(80.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFA874FF), Color(0xFFFFB26B))
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .clickable { onStartQuiz() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start Quiz",
                    modifier = Modifier.size(44.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PLAY",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun QuizSelectionScreen(
    onGradeSelected: (Int, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFBF2), Color(0xFFF8F5EC))
                )
            )
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select Grade",
            fontSize = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF5A4A3B)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Choose your level to begin the quiz",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF7B6A58),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(36.dp))

        BigGradeButton(
            text = "Grade 3",
            color = Color(0xFFA874FF),
            onClick = { onGradeSelected(3, "Grade 3") }
        )

        Spacer(modifier = Modifier.height(18.dp))

        BigGradeButton(
            text = "Grade 4",
            color = Color(0xFFFFB26B),
            onClick = { onGradeSelected(4, "Grade 4") }
        )

        Spacer(modifier = Modifier.height(18.dp))

        BigGradeButton(
            text = "Grade 5",
            color = Color(0xFF6FE3C1),
            onClick = { onGradeSelected(5, "Grade 5") }
        )
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF2))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Settings",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF5A4A3B)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // User info card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Logged in as:",
                    fontSize = 16.sp,
                    color = Color(0xFF7B6A58)
                )
                Text(
                    text = sessionManager.getUsername() ?: "Guest",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A4A3B)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Logout Button
        Button(
            onClick = {
                sessionManager.logout()
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B))
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "LOGOUT",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun BigGradeButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun FancyNavigationBar(
    currentScreen: String,
    onTabSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentScreen == "Home" || currentScreen == "Dashboard" || currentScreen == "QuizSelection",
                onClick = { onTabSelected("Home") }
            )
            NavBarItem(
                icon = Icons.Default.Star,
                label = "Awards",
                isSelected = currentScreen == "Achievements",
                onClick = { onTabSelected("Achievements") }
            )
            NavBarItem(
                icon = Icons.Default.Person,
                label = "Guardian",
                isSelected = currentScreen == "Guardian",
                onClick = { onTabSelected("Guardian") }
            )
            NavBarItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = currentScreen == "Settings",
                onClick = { onTabSelected("Settings") }
            )
        }
    }
}

@Composable
fun NavBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (isSelected) Color(0xFFA874FF) else Color(0xFFBCB1A4)
    val background = if (isSelected) Color(0xFFA874FF).copy(alpha = 0.1f) else Color.Transparent

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(28.dp)
        )
        if (isSelected) {
            Text(
                text = label,
                color = tint,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "$name Screen coming soon!", fontSize = 20.sp, color = Color(0xFF7B6A58))
    }
}
