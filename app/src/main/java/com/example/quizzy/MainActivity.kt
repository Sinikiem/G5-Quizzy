package com.example.quizzy

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.quizzy.network.NetworkClient
import com.example.quizzy.ui.theme.QuizzyTheme
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private object QuizzyUI {
    val ColorPurple = Color(0xFFA874FF)
    val ColorOrange = Color(0xFFFFB26B)
    val ColorGreen = Color(0xFFC5FFD8)
    val ColorRed = Color(0xFFFF6B6B)
    val ColorDarkGrey = Color(0xFF9E9E9E)
    val ColorLightGrey = Color(0xFFBDBDBD)

    val PaddingLarge = 24.dp
    val PaddingMedium = 16.dp
    val PaddingSmall = 8.dp
    val CornerRadiusLarge = 28.dp
    val CornerRadiusMedium = 24.dp
    val CornerRadiusSmall = 16.dp

    const val ChartLabelTextSize = 18f
    const val ChartValueTextSize = 22f
    const val ChartLegendTextSize = 18f
    const val ChartLegendFormSize = 14f
    const val PieSliceSpace = 4f
    const val PieHoleRadius = 45f
}

data class GuardianQuizSession(
    val id: Int,
    val displaySessionNumber: Int = 0,
    val score: Int,
    val completedAt: String,
    val totalQuestions: Int
)

private const val NAV_PREFS = "quizzy_navigation_state"
private const val KEY_LAST_MAIN_SCREEN = "last_main_screen"
private const val KEY_GUARDIAN_FILTER = "guardian_filter"

private fun getNavPrefs(context: Context) =
    context.getSharedPreferences(NAV_PREFS, Context.MODE_PRIVATE)

private fun saveLastMainScreen(context: Context, screen: String) {
    getNavPrefs(context).edit().putString(KEY_LAST_MAIN_SCREEN, screen).apply()
}

private fun getLastMainScreen(context: Context): String {
    return getNavPrefs(context).getString(KEY_LAST_MAIN_SCREEN, "Home") ?: "Home"
}

private fun saveGuardianFilter(context: Context, filter: String) {
    getNavPrefs(context).edit().putString(KEY_GUARDIAN_FILTER, filter).apply()
}

private fun getGuardianFilter(context: Context): String {
    return getNavPrefs(context).getString(KEY_GUARDIAN_FILTER, "Last 15") ?: "Last 15"
}

class MainActivity : ComponentActivity() {

    private var pendingStartScreen by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pendingStartScreen = intent.getStringExtra("start_screen")

        MusicManager.startMusic(this)

        val sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()

        setContent {
            val systemInDarkTheme = isSystemInDarkTheme()
            var isDarkMode by remember {
                mutableStateOf(sessionManager.isDarkMode(systemInDarkTheme))
            }

            QuizzyTheme(darkTheme = isDarkMode) {
                var currentScreen by remember {
                    mutableStateOf(getLastMainScreen(this@MainActivity))
                }

                LaunchedEffect(pendingStartScreen) {
                    val startScreen = pendingStartScreen
                    if (!startScreen.isNullOrBlank()) {
                        currentScreen = startScreen
                        saveLastMainScreen(this@MainActivity, startScreen)
                        pendingStartScreen = null
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        bottomBar = {
                            FancyNavigationBar(
                                currentScreen = currentScreen,
                                onTabSelected = { screen ->
                                    if (screen == "Achievements") {
                                        saveLastMainScreen(this@MainActivity, currentScreen)
                                        val achievementsIntent = Intent(
                                            this@MainActivity,
                                            AchievementsActivity::class.java
                                        )
                                        achievementsIntent.putExtra("start_screen", currentScreen)
                                        startActivity(achievementsIntent)
                                    } else {
                                        currentScreen = screen
                                        saveLastMainScreen(this@MainActivity, screen)
                                    }
                                }
                            )
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            MainNavigationContent(
                                currentScreen = currentScreen,
                                isDarkMode = isDarkMode,
                                sessionManager = sessionManager,
                                onScreenChange = { currentScreen = it },
                                onThemeChanged = { dark ->
                                    isDarkMode = dark
                                    sessionManager.setThemeMode(dark)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MusicManager.startMusic(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingStartScreen = intent.getStringExtra("start_screen")
    }
}

@Composable
private fun MainNavigationContent(
    currentScreen: String,
    isDarkMode: Boolean,
    sessionManager: SessionManager,
    onScreenChange: (String) -> Unit,
    onThemeChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    when (currentScreen) {
        "Home", "Dashboard" -> DashboardScreen(
            onStartQuiz = {
                onScreenChange("QuizSelection")
                saveLastMainScreen(context, "QuizSelection")
            }
        )

        "QuizSelection" -> QuizSelectionScreen(
            onGradeSelected = { gradeLevel, gradeName ->
                val instructionsIntent = Intent(context, InstructionsActivity::class.java)
                instructionsIntent.putExtra("GRADE_LEVEL", gradeLevel)
                instructionsIntent.putExtra("GRADE_NAME", gradeName)
                context.startActivity(instructionsIntent)
            }
        )

        "Guardian" -> GuardianDashboardScreen()

        "Settings" -> SettingsScreen(
            isDarkMode = isDarkMode,
            onThemeChanged = onThemeChanged
        )

        // Study Plan tab — launches StudyPlanActivity and returns to Home
        "StudyPlan" -> {
            LaunchedEffect(Unit) {
                val intent = Intent(context, StudyPlanActivity::class.java).apply {
                    putExtra("topic", "General Math")
                    putExtra("accuracy", 0.5)
                    putExtra("gradeLevel", 3)
                }
                context.startActivity(intent)
                onScreenChange("Home")
                saveLastMainScreen(context, "Home")
            }
        }

        else -> DashboardScreen(
            onStartQuiz = {
                onScreenChange("QuizSelection")
                saveLastMainScreen(context, "QuizSelection")
            }
        )
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
            if (result.isSuccess) {
                val json = result.getOrNull()
                totalScore = json?.optInt("totalScore", 0) ?: 0
            } else {
                totalScore = 0
            }
        } catch (_: Exception) {
            totalScore = 0
        } finally {
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
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(QuizzyUI.PaddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        AppNameHeader()

        Spacer(modifier = Modifier.height(QuizzyUI.PaddingSmall))

        Text(
            text = "Ready to test your knowledge?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(QuizzyUI.PaddingLarge))

        if (isLoading) {
            CircularProgressIndicator(color = QuizzyUI.ColorPurple)
        } else {
            ScoreCard(totalScore = totalScore)
        }

        Spacer(modifier = Modifier.height(QuizzyUI.PaddingMedium))

        // Info chips row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InfoChip(
                modifier = Modifier.weight(1f),
                label = "Grades",
                value = "3 – 5",
                color = QuizzyUI.ColorPurple
            )
            InfoChip(
                modifier = Modifier.weight(1f),
                label = "Per Quiz",
                value = "5 Qs",
                color = QuizzyUI.ColorOrange
            )
            InfoChip(
                modifier = Modifier.weight(1f),
                label = "Study Plan",
                value = "AI",
                color = QuizzyUI.ColorGreen
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        PlayButton(scale = scale, onClick = onStartQuiz)

        Spacer(modifier = Modifier.height(14.dp))

        // Study Plan button — same gradient as PLAY
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(QuizzyUI.CornerRadiusMedium))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(QuizzyUI.ColorPurple, QuizzyUI.ColorOrange)
                    ),
                    shape = RoundedCornerShape(QuizzyUI.CornerRadiusMedium)
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
        shape = RoundedCornerShape(QuizzyUI.CornerRadiusSmall),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
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
private fun AppNameHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Q",
            fontSize = 80.sp,
            fontWeight = FontWeight.ExtraBold,
            color = QuizzyUI.ColorPurple
        )
        Text(
            text = "uizzy",
            fontSize = 64.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun ScoreCard(totalScore: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(QuizzyUI.CornerRadiusLarge))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(QuizzyUI.ColorPurple, QuizzyUI.ColorOrange)
                ),
                shape = RoundedCornerShape(QuizzyUI.CornerRadiusLarge)
            )
            .padding(QuizzyUI.PaddingLarge)
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
                    fontWeight = FontWeight.Medium
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

@Composable
private fun PlayButton(scale: Float, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(200.dp)
            .height(72.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(QuizzyUI.CornerRadiusLarge))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(QuizzyUI.ColorPurple, QuizzyUI.ColorOrange)
                ),
                shape = RoundedCornerShape(QuizzyUI.CornerRadiusLarge)
            )
            .clickable { onClick() },
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
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
            .padding(horizontal = QuizzyUI.PaddingLarge, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select Grade",
            fontSize = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Choose your level to begin the quiz",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(36.dp))

        BigGradeButton(text = "Grade 3", color = QuizzyUI.ColorPurple, onClick = { onGradeSelected(3, "Grade 3") })
        Spacer(modifier = Modifier.height(18.dp))
        BigGradeButton(text = "Grade 4", color = QuizzyUI.ColorOrange, onClick = { onGradeSelected(4, "Grade 4") })
        Spacer(modifier = Modifier.height(18.dp))
        BigGradeButton(text = "Grade 5", color = QuizzyUI.ColorGreen, onClick = { onGradeSelected(5, "Grade 5") })
    }
}

private fun parseSessionDate(dateString: String): Date? {
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss",
        "MMM d, yyyy - h:mm a"
    )
    for (pattern in patterns) {
        try {
            val formatter = SimpleDateFormat(pattern, Locale.getDefault())
            if (pattern.contains("'Z'") || pattern.contains("XXX")) {
                formatter.timeZone = TimeZone.getTimeZone("UTC")
            }
            return formatter.parse(dateString)
        } catch (_: Exception) { }
    }
    return null
}

private fun formatSessionDate(dateString: String): String {
    val date = parseSessionDate(dateString) ?: return dateString
    return SimpleDateFormat("d - MMM - yyyy", Locale.getDefault()).format(date)
}

private fun formatShortChartDate(dateString: String): String {
    val date = parseSessionDate(dateString) ?: return dateString
    return SimpleDateFormat("d MMM", Locale.getDefault()).format(date)
}

private fun isInCurrentDay(date: Date): Boolean {
    val now = Calendar.getInstance()
    val sessionCal = Calendar.getInstance().apply { time = date }
    return now.get(Calendar.YEAR) == sessionCal.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == sessionCal.get(Calendar.DAY_OF_YEAR)
}

private fun isInCurrentWeek(date: Date): Boolean {
    val now = Calendar.getInstance()
    val sessionCal = Calendar.getInstance().apply { time = date }
    return now.get(Calendar.YEAR) == sessionCal.get(Calendar.YEAR) &&
            now.get(Calendar.WEEK_OF_YEAR) == sessionCal.get(Calendar.WEEK_OF_YEAR)
}

private fun isInCurrentMonth(date: Date): Boolean {
    val now = Calendar.getInstance()
    val sessionCal = Calendar.getInstance().apply { time = date }
    return now.get(Calendar.YEAR) == sessionCal.get(Calendar.YEAR) &&
            now.get(Calendar.MONTH) == sessionCal.get(Calendar.MONTH)
}

private fun applySessionFilter(
    sessions: List<GuardianQuizSession>,
    filter: String
): List<GuardianQuizSession> {
    val sortedSessions = sessions.sortedBy { parseSessionDate(it.completedAt)?.time ?: 0L }
    return when (filter) {
        "Last 15" -> sortedSessions.takeLast(15)
        "Current Day" -> sortedSessions.filter { parseSessionDate(it.completedAt)?.let { d -> isInCurrentDay(d) } == true }
        "Current Week" -> sortedSessions.filter { parseSessionDate(it.completedAt)?.let { d -> isInCurrentWeek(d) } == true }
        "Current Month" -> sortedSessions.filter { parseSessionDate(it.completedAt)?.let { d -> isInCurrentMonth(d) } == true }
        else -> sortedSessions.takeLast(15)
    }
}

@Composable
fun GuardianDashboardScreen() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    var totalScore by remember { mutableIntStateOf(0) }
    var allSessions by remember { mutableStateOf<List<GuardianQuizSession>>(emptyList()) }
    var displayedSessions by remember { mutableStateOf<List<GuardianQuizSession>>(emptyList()) }
    var selectedFilter by remember { mutableStateOf(getGuardianFilter(context)) }
    var selectedDisplay by remember { mutableStateOf(sessionManager.getSelectedDisplay() ?: "Latest Activity") }
    var allBadges by remember { mutableStateOf<List<Badges>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    val userId = sessionManager.getUserId()
    val filterOptions = listOf("Last 15", "Current Day", "Current Week", "Current Month")
    val displayOptions = listOf("Latest Activity", "Charts", "Achievements")

    LaunchedEffect(Unit) {
        try {
            val scoreResult = NetworkClient.get("/score/user/$userId")
            if (scoreResult.isSuccess) {
                totalScore = scoreResult.getOrNull()?.optInt("totalScore", 0) ?: 0
            }

            val sessionsResult = NetworkClient.get("/guardian/$userId/latest-sessions")
            if (sessionsResult.isSuccess) {
                val json = sessionsResult.getOrNull()
                val loadedSessions = mutableListOf<GuardianQuizSession>()
                val sessionsArray = if (json != null && json.has("data")) json.getJSONArray("data") else null
                if (sessionsArray != null) {
                    for (i in 0 until sessionsArray.length()) {
                        val item = sessionsArray.getJSONObject(i)
                        loadedSessions.add(
                            GuardianQuizSession(
                                id = item.optInt("id", 0),
                                displaySessionNumber = item.optInt("displaySessionNumber", i + 1),
                                score = item.optInt("finalscore", 0),
                                completedAt = item.optString("completion", "Unknown"),
                                totalQuestions = 5
                            )
                        )
                    }
                }
                allSessions = loadedSessions
                displayedSessions = applySessionFilter(loadedSessions, selectedFilter)
            } else {
                errorMessage = "Failed to load latest sessions"
            }

            QuizRepository.getUserBadges(userId.toInt(), object : QuizRepository.BadgeCallback {
                override fun onSuccess(earnedBadges: List<Badges>) {
                    Handler(Looper.getMainLooper()).post {
                        allBadges = BadgeManager.mergeBadgeStates(BadgeCatalog.getAllBadges(), earnedBadges)
                    }
                }
                override fun onError(error: String) {
                    Handler(Looper.getMainLooper()).post {
                        if (allBadges.isEmpty()) allBadges = BadgeCatalog.getAllBadges()
                    }
                }
            })
        } catch (e: Exception) {
            errorMessage = "Something went wrong"
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Gradient header banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(QuizzyUI.ColorPurple, QuizzyUI.ColorOrange)
                    )
                )
                .padding(QuizzyUI.PaddingLarge),
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(QuizzyUI.PaddingLarge)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = QuizzyUI.ColorPurple)
                }
            } else if (errorMessage.isNotBlank()) {
                Text(text = errorMessage, color = QuizzyUI.ColorRed, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            } else {
                GuardianSummaryHeader(totalScore = totalScore)

                Spacer(modifier = Modifier.height(20.dp))

                GuardianControls(
                    selectedFilter = selectedFilter,
                    selectedDisplay = selectedDisplay,
                    filterOptions = filterOptions,
                    displayOptions = displayOptions,
                    onFilterChange = {
                        selectedFilter = it
                        saveGuardianFilter(context, it)
                        displayedSessions = applySessionFilter(allSessions, it)
                    },
                    onDisplayChange = {
                        selectedDisplay = it
                        sessionManager.saveSelectedDisplay(it)
                    }
                )

                Spacer(modifier = Modifier.height(QuizzyUI.PaddingMedium))

                when (selectedDisplay) {
                    "Latest Activity" -> LatestActivityView(displayedSessions, allSessions, selectedFilter)
                    "Charts" -> GuardianChartsView(allSessions = displayedSessions)
                    "Achievements" -> GuardianAchievementsView(allBadges = allBadges)
                }
            }
        }
    }
}

@Composable
private fun GuardianSummaryHeader(totalScore: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(QuizzyUI.CornerRadiusMedium),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(QuizzyUI.PaddingLarge)) {
            Text(text = "Student Progress:", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(QuizzyUI.PaddingMedium))
            Text(text = "Total Score: $totalScore", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = QuizzyUI.ColorPurple)
        }
    }
}

@Composable
private fun GuardianControls(
    selectedFilter: String,
    selectedDisplay: String,
    filterOptions: List<String>,
    displayOptions: List<String>,
    onFilterChange: (String) -> Unit,
    onDisplayChange: (String) -> Unit
) {
    var isFilterDropdownExpanded by remember { mutableStateOf(false) }
    var isDisplayDropdownExpanded by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.weight(1f)) {
            Button(
                onClick = { isFilterDropdownExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(QuizzyUI.CornerRadiusSmall),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(text = "Filter: $selectedFilter", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = isFilterDropdownExpanded, onDismissRequest = { isFilterDropdownExpanded = false }) {
                filterOptions.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { onFilterChange(option); isFilterDropdownExpanded = false })
                }
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            Button(
                onClick = { isDisplayDropdownExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(QuizzyUI.CornerRadiusSmall),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(text = "View: $selectedDisplay", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = isDisplayDropdownExpanded, onDismissRequest = { isDisplayDropdownExpanded = false }) {
                displayOptions.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { onDisplayChange(option); isDisplayDropdownExpanded = false })
                }
            }
        }
    }
}

@Composable
private fun LatestActivityView(
    displayedSessions: List<GuardianQuizSession>,
    allSessions: List<GuardianQuizSession>,
    selectedFilter: String
) {
    Column {
        Text(text = "Latest Activity", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(QuizzyUI.PaddingMedium))
        if (displayedSessions.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = QuizzyUI.PaddingMedium), contentAlignment = Alignment.Center) {
                Text(
                    text = if (allSessions.isEmpty()) "No quiz sessions found." else "No sessions found for $selectedFilter.",
                    fontSize = 16.sp, color = Color.Gray
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(displayedSessions) { index, session ->
                    SessionItemCard(index, session)
                }
            }
        }
    }
}

@Composable
private fun SessionItemCard(index: Int, session: GuardianQuizSession) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(QuizzyUI.PaddingMedium)) {
            Text(text = "Session ${index + 1}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Score: ${session.score}", fontSize = 16.sp, color = QuizzyUI.ColorPurple, fontWeight = FontWeight.SemiBold)
            Text(text = "Total Questions: ${session.totalQuestions}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Completed: ${formatSessionDate(session.completedAt)}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun GuardianAchievementsView(allBadges: List<Badges>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Achievements", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(QuizzyUI.PaddingMedium))
        if (allBadges.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = QuizzyUI.PaddingLarge), contentAlignment = Alignment.Center) {
                Text(text = "No badges available yet.", fontSize = 16.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = QuizzyUI.PaddingMedium),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(allBadges) { _, badge -> BadgeItemCard(badge) }
            }
        }
    }
}

@Composable
private fun BadgeItemCard(badge: Badges) {
    val unlocked = badge.isUnlocked
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(QuizzyUI.PaddingMedium), verticalAlignment = Alignment.CenterVertically) {
            Text(text = if (unlocked) "🏆" else "🔒", fontSize = 28.sp)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = badge.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (unlocked) MaterialTheme.colorScheme.onSurface else QuizzyUI.ColorDarkGrey)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = badge.description, fontSize = 14.sp, color = if (unlocked) MaterialTheme.colorScheme.onSurfaceVariant else QuizzyUI.ColorLightGrey)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = if (unlocked) "Unlocked" else "Locked", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (unlocked) Color(0xFF2E7D32) else QuizzyUI.ColorDarkGrey)
            }
        }
    }
}

@Composable
fun GuardianChartsView(allSessions: List<GuardianQuizSession>) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var selectedChart by remember { mutableStateOf(sessionManager.getSelectedChart() ?: "Pie Chart") }
    val chartTypes = listOf("Pie Chart", "Bar Chart", "Line Chart")

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp), contentAlignment = Alignment.Center) {
            when {
                allSessions.isEmpty() -> Text(text = "No chart data available.", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                selectedChart == "Line Chart" -> GuardianLineChartView(sessions = allSessions)
                selectedChart == "Bar Chart" -> GuardianBarChartView(sessions = allSessions)
                selectedChart == "Pie Chart" -> GuardianPieChartView(sessions = allSessions)
            }
        }
        ChartSelectionOverlay(selectedChart = selectedChart, chartTypes = chartTypes, onChartSelected = { selectedChart = it; sessionManager.saveSelectedChart(it) })
    }
}

@Composable
private fun BoxScope.ChartSelectionOverlay(
    selectedChart: String,
    chartTypes: List<String>,
    onChartSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(start = QuizzyUI.PaddingLarge, end = QuizzyUI.PaddingLarge, bottom = 12.dp),
        shape = RoundedCornerShape(QuizzyUI.CornerRadiusMedium),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            chartTypes.forEach { type ->
                val isSelected = selectedChart == type
                Column(
                    modifier = Modifier.clip(RoundedCornerShape(QuizzyUI.CornerRadiusSmall))
                        .background(if (isSelected) QuizzyUI.ColorPurple.copy(alpha = 0.22f) else Color.Transparent)
                        .clickable { onChartSelected(type) }
                        .padding(horizontal = QuizzyUI.PaddingMedium, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = type.split(" ")[0], color = if (isSelected) QuizzyUI.ColorPurple else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun GuardianBarChartView(sessions: List<GuardianQuizSession>) {
    val sortedSessions = remember(sessions) { sessions.sortedBy { parseSessionDate(it.completedAt)?.time ?: 0L } }
    val entries = remember(sortedSessions) { sortedSessions.mapIndexed { i, s -> BarEntry(i.toFloat(), s.score.toFloat()) } }
    val xLabels = remember(sortedSessions) { sortedSessions.map { formatShortChartDate(it.completedAt) } }
    val chartBarColor = QuizzyUI.ColorPurple.toArgb()
    val chartTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val chartGridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth().height(300.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Score", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.graphicsLayer { rotationZ = -90f })
            AndroidView(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                factory = { context ->
                    BarChart(context).apply {
                        description.isEnabled = false; legend.isEnabled = false; axisRight.isEnabled = false
                        xAxis.apply { position = XAxis.XAxisPosition.BOTTOM; textColor = chartTextColor; setDrawGridLines(false); labelRotationAngle = -20f; valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() { override fun getFormattedValue(value: Float) = xLabels.getOrElse(value.toInt()) { "" } } }
                        axisLeft.apply { axisMinimum = 0f; textColor = chartTextColor; gridColor = chartGridColor }
                    }
                },
                update = { chart ->
                    val dataSet = BarDataSet(entries, "Quiz Scores").apply { color = chartBarColor; valueTextColor = chartTextColor }
                    chart.data = BarData(dataSet).apply { barWidth = 0.6f }; chart.invalidate()
                }
            )
        }
        Text(text = "Date", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun GuardianPieChartView(sessions: List<GuardianQuizSession>) {
    val totalCorrect = sessions.sumOf { it.score }
    val totalIncorrect = sessions.sumOf { it.totalQuestions } - totalCorrect
    val entries = listOf(PieEntry(totalCorrect.toFloat(), "Correct"), PieEntry(totalIncorrect.toFloat(), "Incorrect"))
    val colors = listOf(Color.Green.toArgb(), QuizzyUI.ColorRed.toArgb())
    val chartTextColor = MaterialTheme.colorScheme.onSurface.toArgb()

    AndroidView(
        modifier = Modifier.fillMaxWidth().height(320.dp),
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false; isDrawHoleEnabled = true; setHoleColor(android.graphics.Color.TRANSPARENT)
                setEntryLabelColor(android.graphics.Color.WHITE); setEntryLabelTextSize(QuizzyUI.ChartLabelTextSize); setEntryLabelTypeface(Typeface.DEFAULT_BOLD)
                legend.apply { textColor = chartTextColor; textSize = QuizzyUI.ChartLegendTextSize; formSize = QuizzyUI.ChartLegendFormSize; horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER; verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM; orientation = Legend.LegendOrientation.HORIZONTAL; setDrawInside(false) }
                setUsePercentValues(false); setDrawEntryLabels(true); holeRadius = QuizzyUI.PieHoleRadius; transparentCircleRadius = QuizzyUI.PieHoleRadius + 5f; centerText = ""
            }
        },
        update = { chart ->
            val dataSet = PieDataSet(entries, "").apply { this.colors = colors; valueTextColor = android.graphics.Color.WHITE; valueTextSize = QuizzyUI.ChartValueTextSize; valueTypeface = Typeface.DEFAULT_BOLD; xValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE; yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE; sliceSpace = QuizzyUI.PieSliceSpace }
            chart.data = PieData(dataSet).apply { setValueFormatter(object : com.github.mikephil.charting.formatter.ValueFormatter() { override fun getFormattedValue(value: Float) = value.toInt().toString() }) }; chart.invalidate()
        }
    )
}

@Composable
fun GuardianLineChartView(sessions: List<GuardianQuizSession>) {
    val sortedSessions = remember(sessions) { sessions.sortedBy { parseSessionDate(it.completedAt)?.time ?: 0L } }
    val entries = remember(sortedSessions) { sortedSessions.mapIndexed { i, s -> Entry(i.toFloat(), s.score.toFloat()) } }
    val xLabels = remember(sortedSessions) { sortedSessions.map { formatShortChartDate(it.completedAt) } }
    val chartLineColor = QuizzyUI.ColorPurple.toArgb()
    val chartTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val chartGridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth().height(300.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Score", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.graphicsLayer { rotationZ = -90f })
            AndroidView(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                factory = { context ->
                    LineChart(context).apply {
                        description.isEnabled = false; legend.isEnabled = false; axisRight.isEnabled = false
                        xAxis.apply { position = XAxis.XAxisPosition.BOTTOM; textColor = chartTextColor; setDrawGridLines(false); labelRotationAngle = -20f; valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() { override fun getFormattedValue(value: Float) = xLabels.getOrElse(value.toInt()) { "" } } }
                        axisLeft.apply { axisMinimum = 0f; textColor = chartTextColor; gridColor = chartGridColor }
                    }
                },
                update = { chart ->
                    val dataSet = LineDataSet(entries, "Quiz Scores").apply { color = chartLineColor; setCircleColor(chartLineColor); lineWidth = 2.5f; valueTextColor = chartTextColor }
                    chart.data = LineData(dataSet); chart.invalidate()
                }
            )
        }
        Text(text = "Date", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onThemeChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val scrollState = rememberScrollState()
    val prefs = context.getSharedPreferences("quizzy_settings", Context.MODE_PRIVATE)

    var volume by remember { mutableStateOf(prefs.getFloat("music_volume", 0.2f)) }
    var sfxVolume by remember { mutableStateOf(prefs.getFloat("sfx_volume", 0.7f)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(QuizzyUI.ColorPurple, QuizzyUI.ColorOrange)
                    )
                )
                .padding(QuizzyUI.PaddingLarge)
        ) {
            Text(text = "Settings", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }

        Column(
            modifier = Modifier.padding(QuizzyUI.PaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            UserInfoCard(username = sessionManager.getUsername() ?: "Guest")

            Spacer(modifier = Modifier.height(QuizzyUI.PaddingLarge))

            VolumeControlCard(
                label = "Music Volume",
                value = volume,
                onValueChange = {
                    volume = it
                    MusicManager.setVolume(context, it)
                    prefs.edit().putFloat("music_volume", it).apply()
                }
            )

            Spacer(modifier = Modifier.height(QuizzyUI.PaddingLarge))

            VolumeControlCard(
                label = "Sound Effects Volume",
                value = sfxVolume,
                onValueChange = {
                    sfxVolume = it
                    MusicManager.setSFXVolume(context, it)
                    prefs.edit().putFloat("sfx_volume", it).apply()
                }
            )

            Spacer(modifier = Modifier.height(QuizzyUI.PaddingLarge))

            ThemeToggleCard(isDarkMode = isDarkMode, onThemeChanged = onThemeChanged)

            Spacer(modifier = Modifier.height(QuizzyUI.PaddingLarge))

            // About card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(QuizzyUI.CornerRadiusMedium),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(QuizzyUI.PaddingLarge)) {
                    Text(text = "About Quizzy", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Quizzy helps children in Grades 3–5 practice math through fun, gamified quizzes. Study plans are personalized using AI based on quiz performance.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = RoundedCornerShape(8.dp), color = QuizzyUI.ColorPurple.copy(alpha = 0.1f)) {
                            Text(text = "Grades 3–5", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = QuizzyUI.ColorPurple, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = QuizzyUI.ColorOrange.copy(alpha = 0.1f)) {
                            Text(text = "AI Powered", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = QuizzyUI.ColorOrange, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            LogoutButton(onClick = {
                sessionManager.logout()
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            })

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun UserInfoCard(username: String) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(QuizzyUI.CornerRadiusMedium), color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
        Column(modifier = Modifier.padding(QuizzyUI.PaddingLarge)) {
            Text(text = "Logged in as:", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = username, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun VolumeControlCard(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(QuizzyUI.CornerRadiusMedium), color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
        Column(modifier = Modifier.padding(QuizzyUI.PaddingLarge)) {
            Text(text = label, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            androidx.compose.material3.Slider(value = value, onValueChange = onValueChange, valueRange = 0f..1f)
            Text(text = "${(value * 100).toInt()}%", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ThemeToggleCard(isDarkMode: Boolean, onThemeChanged: (Boolean) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(QuizzyUI.CornerRadiusMedium), color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
        Column(modifier = Modifier.padding(QuizzyUI.PaddingLarge), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Theme", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(imageVector = Icons.Default.WbSunny, contentDescription = null, tint = if (!isDarkMode) QuizzyUI.ColorOrange else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Switch(checked = isDarkMode, onCheckedChange = onThemeChanged)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(imageVector = Icons.Default.NightsStay, contentDescription = null, tint = if (isDarkMode) QuizzyUI.ColorPurple else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(QuizzyUI.CornerRadiusMedium), colors = ButtonDefaults.buttonColors(containerColor = QuizzyUI.ColorRed)) {
        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = "Logout", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun BigGradeButton(text: String, color: Color, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(88.dp), shape = RoundedCornerShape(QuizzyUI.CornerRadiusMedium), colors = ButtonDefaults.buttonColors(containerColor = color), elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)) {
        Text(text = text, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun FancyNavigationBar(
    currentScreen: String,
    onTabSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(QuizzyUI.PaddingMedium),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(icon = Icons.Default.Home, label = "Home", isSelected = currentScreen == "Home" || currentScreen == "Dashboard" || currentScreen == "QuizSelection", onClick = { onTabSelected("Home") })
            NavBarItem(icon = Icons.Default.Star, label = "Awards", isSelected = false, onClick = { onTabSelected("Achievements") })
            NavBarItem(icon = Icons.Default.MenuBook, label = "Plan", isSelected = currentScreen == "StudyPlan", onClick = { onTabSelected("StudyPlan") })
            NavBarItem(icon = Icons.Default.Person, label = "Guardian", isSelected = currentScreen == "Guardian", onClick = { onTabSelected("Guardian") })
            NavBarItem(icon = Icons.Default.Settings, label = "Settings", isSelected = currentScreen == "Settings", onClick = { onTabSelected("Settings") })
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
    val tint = if (isSelected) QuizzyUI.ColorPurple else MaterialTheme.colorScheme.onSurfaceVariant
    val background = if (isSelected) QuizzyUI.ColorPurple.copy(alpha = 0.1f) else Color.Transparent

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(QuizzyUI.CornerRadiusSmall))
            .background(background)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(26.dp))
        if (isSelected) {
            Text(text = label, color = tint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}