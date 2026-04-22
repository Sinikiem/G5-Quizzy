package com.example.quizzy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizzy.ui.theme.QuizzyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Constants for network configuration and UI styling in Instructions screen.
 */
private object InstructionsUI {
    val BackgroundLight = Color(0xFFFFFBF2)
    val BackgroundDark = Color(0xFF121212)
    val TextPrimaryLight = Color(0xFF5A4A3B)
    val ProgressColor = Color(0xFFA874FF)
    
    val PaddingScreen = 20.dp
    val PaddingBackBtn = 8.dp
    
    const val TAG = "QUIZZY_DEBUG"
    const val BACKEND_BASE_URL = "http://10.0.2.2:3000/api"
    const val TIMEOUT_MILLIS = 15000
}

/**
 * Intermediate Activity that handles AI quiz generation and setup before the quiz begins.
 * It fetches instruction text and triggers the backend to generate questions based on the grade level.
 */
class InstructionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val gradeLevel = intent.getIntExtra("GRADE_LEVEL", 3)
        val sessionManager = SessionManager(this)

        setContent {
            val systemInDarkTheme = isSystemInDarkTheme()
            val isDarkMode = sessionManager.isDarkMode(systemInDarkTheme)

            QuizzyTheme(darkTheme = isDarkMode) {
                InstructionsScreen(
                    gradeLevel = gradeLevel,
                    isDarkMode = isDarkMode,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

/**
 * Internal data model for questions received from the AI generation service.
 */
data class DisplayQuestion(
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String
)

/**
 * Main screen for the Instructions Activity.
 * Displays a loading indicator while the AI generates the quiz.
 * 
 * Logic flow:
 * 1. Build a prompt based on the selected grade level.
 * 2. Fetch instructions and generated questions from the backend.
 * 3. Store questions in [QuizRepository] and navigate to [QuizActivity].
 */
@Composable
fun InstructionsScreen(
    gradeLevel: Int,
    isDarkMode: Boolean,
    onBackClick: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    LaunchedEffect(gradeLevel) {
        isLoading = true
        errorMessage = ""

        try {
            // Feature: Print selected level to console when creating questions
            System.out.println("Creating questions for Grade Level: $gradeLevel")

            // Optional instruction text (can be shown if UI supports it in future)
            fetchInstructionFromBackend(gradeLevel)

            val prompt = buildPromptForGrade(gradeLevel)
            val userId = sessionManager.getUserId().toInt()
            
            val (questions, sessionId) = fetchGeneratedQuizFromBackend(prompt, userId, gradeLevel)
            
            if (questions.isNotEmpty()) {
                // Feature: Log generated questions to standard console output with specific format
                printQuestionsToConsole(questions)
                
                setupQuizSession(questions, sessionId)
                
                // Navigate to the actual quiz
                val intent = Intent(context, QuizActivity::class.java)
                intent.putExtra("GRADE_LEVEL", gradeLevel)
                context.startActivity(intent)
                (context as? InstructionsActivity)?.finish()
            } else {
                errorMessage = "Oops, something went wrong."
            }

        } catch (e: Exception) {
            errorMessage = "Oops, something went wrong."
            Log.e(InstructionsUI.TAG, "Screen load failed", e)
        } finally {
            isLoading = false
        }
    }

    val backgroundColor = if (isDarkMode) InstructionsUI.BackgroundDark else InstructionsUI.BackgroundLight
    val textColor = if (isDarkMode) Color.White else InstructionsUI.TextPrimaryLight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(InstructionsUI.PaddingScreen)
    ) {
        Text(
            text = "← Back",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier
                .align(Alignment.TopStart)
                .clickable { onBackClick() }
                .padding(vertical = InstructionsUI.PaddingBackBtn, horizontal = 4.dp)
        )

        when {
            isLoading -> {
                CircularProgressIndicator(
                    color = InstructionsUI.ProgressColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            errorMessage.isNotEmpty() -> {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * Maps the raw AI questions to the repository for global access during the quiz.
 */
private fun setupQuizSession(questions: List<DisplayQuestion>, sessionId: Long) {
    QuizRepository.currentQuizQuestions = questions.map { dq ->
        Question(
            dq.questionText,
            dq.options.getOrNull(0) ?: "",
            dq.options.getOrNull(1) ?: "",
            dq.options.getOrNull(2) ?: "",
            dq.options.getOrNull(3) ?: "",
            dq.correctAnswer
        )
    }
    QuizRepository.currentSessionId = sessionId
}

/**
 * Algorithm: Generates a specific natural language prompt for the AI based on the student's grade level.
 * This ensures the generated math problems are age-appropriate.
 */
fun buildPromptForGrade(gradeLevel: Int): String {
    return when (gradeLevel) {
        3 -> "Generate exactly 5 multiple-choice math questions for Grade 3 students. Focus on addition, subtraction, simple multiplication, and basic word problems."
        4 -> "Generate exactly 5 multiple-choice math questions for Grade 4 students. Focus on multiplication, division, place value, fractions, and word problems."
        5 -> "Generate exactly 5 multiple-choice math questions for Grade 5 students. Focus on fractions, decimals, multiplication, division, and multi-step word problems."
        else -> "Generate exactly 5 multiple-choice math questions for Grade 3 students."
    }
}

/**
 * Network Call: Retrieves instruction text from the backend.
 */
suspend fun fetchInstructionFromBackend(gradeLevel: Int): String {
    return withContext(Dispatchers.IO) {
        val url = URL("${InstructionsUI.BACKEND_BASE_URL}/instructions/$gradeLevel")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = InstructionsUI.TIMEOUT_MILLIS
        connection.readTimeout = InstructionsUI.TIMEOUT_MILLIS

        try {
            val responseCode = connection.responseCode
            val responseText = readResponseText(connection, responseCode)

            if (responseCode != 200) throw Exception("Request failed: $responseCode")

            val json = JSONObject(responseText)
            json.optString("instructionText", "No instructions found.")
        } finally {
            connection.disconnect()
        }
    }
}

/**
 * Network Call: Triggers the AI quiz generation service.
 * Returns a list of questions and a unique session identifier.
 */
suspend fun fetchGeneratedQuizFromBackend(prompt: String, userId: Int, level: Int): Pair<List<DisplayQuestion>, Long> {
    return withContext(Dispatchers.IO) {
        val encodedPrompt = URLEncoder.encode(prompt, "UTF-8")
        val url = URL("${InstructionsUI.BACKEND_BASE_URL}/quiz/generate?prompt=$encodedPrompt&userId=$userId&level=$level")
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.connectTimeout = InstructionsUI.TIMEOUT_MILLIS
        connection.readTimeout = InstructionsUI.TIMEOUT_MILLIS

        try {
            val responseCode = connection.responseCode
            val responseText = readResponseText(connection, responseCode)

            if (responseCode != 200) throw Exception("Quiz generation failed: $responseCode")

            val json = JSONObject(responseText)
            val sessionId = json.optLong("sessionId", -1L)
            val questionsArray = json.optJSONArray("questions") ?: JSONArray()
            Pair(parseQuestions(questionsArray), sessionId)
        } finally {
            connection.disconnect()
        }
    }
}

/**
 * Utility: Reads the input or error stream from a [HttpURLConnection] and returns a String.
 */
private fun readResponseText(connection: HttpURLConnection, responseCode: Int): String {
    val stream = if (responseCode in 200..299) {
        connection.inputStream
    } else {
        connection.errorStream
    } ?: throw Exception("No response stream from server.")

    val reader = BufferedReader(InputStreamReader(stream))
    val result = StringBuilder()
    var line: String?

    while (true) {
        line = reader.readLine() ?: break
        result.append(line)
    }

    reader.close()
    return result.toString()
}

/**
 * Algorithm: Parses the JSON response from the AI service into a list of [DisplayQuestion] objects.
 * Handles missing fields and filters out blank options.
 */
private fun parseQuestions(questionsArray: JSONArray): List<DisplayQuestion> {
    val questions = mutableListOf<DisplayQuestion>()

    for (i in 0 until questionsArray.length()) {
        val questionObject = questionsArray.getJSONObject(i)

        val questionText = questionObject.optString("questionText", "No question text")
        val option1 = questionObject.optString("option1", "")
        val option2 = questionObject.optString("option2", "")
        val option3 = questionObject.optString("option3", "")
        val option4 = questionObject.optString("option4", "")
        val correctAnswer = questionObject.optString("correctAnswer", "N/A")

        val options = listOf(option1, option2, option3, option4).filter { it.isNotBlank() }

        questions.add(
            DisplayQuestion(
                questionText = questionText,
                options = options,
                correctAnswer = correctAnswer
            )
        )
    }

    return questions
}

/**
 * Algorithm: Formats and prints the generated quiz questions to the standard system output (Console).
 * This uses System.out.println() which appears in the standard process console.
 * The format matches exactly the user's request.
 */
private fun printQuestionsToConsole(questions: List<DisplayQuestion>) {
    if (questions.isEmpty()) return
    
    System.out.println("\n=== GENERATED QUIZ QUESTIONS ===")
    questions.forEachIndexed { index, q ->
        System.out.println("Question ${index + 1}: ${q.questionText}")
        q.options.forEachIndexed { i, opt ->
            System.out.println("  ${i + 1}) $opt")
        }
        System.out.println("  [CORRECT]: ${q.correctAnswer}\n")
    }
    System.out.println("================================\n")
}
