package com.example.quizzy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
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
import com.example.quizzy.network.NetworkClient

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

                                "StudyPlan" -> {
                                    LaunchedEffect(Unit) {
                                        val intent = Intent(this@MainActivity, StudyPlanActivity::class.java).apply {
                                            putExtra("topic", "General Math")
                                            putExtra("accuracy", 0.5)
                                            putExtra("gradeLevel", 3)
                                        }
                                        startActivity(intent)
                                        currentScreen = "Home"
                                    }
                                }

                                "Guardian" -> GuardianDashboardScreen()
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
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var totalScore by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    val userId = sessionManager.getUserId()

    LaunchedEffect(Unit) {
        try {
            val result = NetworkClient.get("/score/user/$userId")
            result.fold(
                onSuccess = { json ->
                    totalScore = json.optInt("totalScore", 0)
                    isLoading = false
                },
                onFailure = {
                    isLoading = false
                }
            )
        } catch (e: Exception) {
            isLoading = false
        }
    }

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
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Q",
                fontSize = 80.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFA874FF)
            )
            Text(
                text = "uizzy",
                fontSize = 64.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF5A4A3B),
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Ready to test your knowledge?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF7B6A58),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Score card
        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFFA874FF))
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(28.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFA874FF), Color(0xFFFFB26B))
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total Score",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = totalScore.toString(),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Keep earning points!",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Score",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick info row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoChip(
                modifier = Modifier.weight(1f),
                label = "Grades",
                value = "3 – 5",
                color = Color(0xFFA874FF)
            )
            InfoChip(
                modifier = Modifier.weight(1f),
                label = "Questions",
                value = "5 per quiz",
                color = Color(0xFFFFB26B)
            )
            InfoChip(
                modifier = Modifier.weight(1f),
                label = "AI Powered",
                value = "Study Plan",
                color = Color(0xFF6FE3C1)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // PLAY button
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(72.dp)
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
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PLAY",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Study Plan button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFA874FF), Color(0xFFFFB26B))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable {
                    val intent = Intent(context, StudyPlanActivity::class.java).apply {
                        putExtra("topic", "General Math")
                        putExtra("accuracy", 0.5)
                        putExtra("gradeLevel", 3)
                    }
                    context.startActivity(intent)
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate Study Plan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.3.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun InfoChip(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = color.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
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
fun GuardianDashboardScreen() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var totalScore by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    val userId = sessionManager.getUserId()

    LaunchedEffect(Unit) {
        try {
            val result = NetworkClient.get("/guardian/$userId/student-score")
            result.fold(
                onSuccess = { json ->
                    totalScore = json.optInt("totalScore", 0)
                    isLoading = false
                },
                onFailure = { error ->
                    Log.e("GUARDIAN", "Fetch failed: ${error.message}")
                    isLoading = false
                }
            )
        } catch (e: Exception) {
            Log.e("GUARDIAN", "Error: ${e.message}")
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F5EC))
            .verticalScroll(rememberScrollState())
    ) {
        // Gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFA874FF), Color(0xFFFFB26B))
                    )
                )
                .padding(28.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text(
                    text = "Guardian Dashboard",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Track your student's progress",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFA874FF))
            }
        } else {
            // Score card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFA874FF).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFA874FF),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Total Score",
                            fontSize = 14.sp,
                            color = Color(0xFF7B6A58),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = totalScore.toString(),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFA874FF)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tip card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(60.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFA874FF), Color(0xFFFFB26B))
                                ),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Encouraging Progress",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5A4A3B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Consistent practice builds strong math skills. Encourage your student to complete quizzes regularly and use the AI Study Plan for personalized guidance.",
                            fontSize = 13.sp,
                            color = Color(0xFF7B6A58),
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // AI study plan prompt card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFA874FF).copy(alpha = 0.07f),
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = Color(0xFFA874FF),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "AI-Powered Study Plan",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA874FF)
                        )
                        Text(
                            text = "Tap 'Plan' in the menu to generate a personalized study plan based on quiz results.",
                            fontSize = 13.sp,
                            color = Color(0xFF7B6A58),
                            lineHeight = 19.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F5EC))
            .verticalScroll(rememberScrollState())
    ) {
        // Gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFA874FF), Color(0xFFFFB26B))
                    )
                )
                .padding(28.dp)
        ) {
            Text(
                text = "Settings",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Account card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFA874FF).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFFA874FF),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Logged in as",
                        fontSize = 13.sp,
                        color = Color(0xFF7B6A58)
                    )
                    Text(
                        text = sessionManager.getUsername() ?: "Guest",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5A4A3B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // App info card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "About Quizzy",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A4A3B)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Quizzy helps children in Grades 3–5 practice math through fun, gamified quizzes. Study plans are personalized using AI based on quiz performance.",
                    fontSize = 13.sp,
                    color = Color(0xFF7B6A58),
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFA874FF).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Grades 3–5",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA874FF),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFB26B).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "AI Powered",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB26B),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Logout button
        Button(
            onClick = {
                sessionManager.logout()
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B))
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Logout",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
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
                .padding(vertical = 12.dp, horizontal = 4.dp),
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
                icon = Icons.Default.MenuBook,
                label = "Plan",
                isSelected = currentScreen == "StudyPlan",
                onClick = { onTabSelected("StudyPlan") }
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
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(26.dp)
        )
        if (isSelected) {
            Text(
                text = label,
                color = tint,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}