package com.example.quizzy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private const val TAG = "QUIZZY_DEBUG"

class InstructionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val gradeLevel = intent.getIntExtra("GRADE_LEVEL", 3)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    InstructionsScreen(
                        gradeLevel = gradeLevel,
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

data class DisplayQuestion(
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String
)

@Composable
fun InstructionsScreen(
    gradeLevel: Int,
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
            val instructionText = fetchInstructionFromBackend(gradeLevel)
            Log.d(TAG, "Retrieved instruction text: $instructionText")

            val prompt = buildPromptForGrade(gradeLevel)
            Log.d(TAG, "Generated questions prompt: $prompt")

            val userId = sessionManager.getUserId().toInt()
            val quizResponse = fetchGeneratedQuizFromBackend(prompt, userId)
            val questions = quizResponse.first
            val sessionId = quizResponse.second
            
            logQuestionsToConsole(questions)

            if (questions.isNotEmpty()) {
                // Map DisplayQuestion to Question (used by QuizActivity)
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

                // Navigate to QuizActivity
                val intent = Intent(context, QuizActivity::class.java)
                context.startActivity(intent)
                (context as? InstructionsActivity)?.finish()
            } else {
                errorMessage = "Oops, something went wrong."
            }

        } catch (e: Exception) {
            errorMessage = "Oops, something went wrong."
            Log.e(TAG, "Screen load failed", e)
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF2))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        Text(
            text = "← Back",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF5A4A3B),
            modifier = Modifier
                .align(Alignment.TopStart)
                .clickable { onBackClick() }
                .padding(vertical = 8.dp, horizontal = 4.dp)
        )

        when {
            isLoading -> {
                CircularProgressIndicator(
                    color = Color(0xFFA874FF),
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

            else -> {
                // intentionally empty
            }
        }
    }
}

fun buildPromptForGrade(gradeLevel: Int): String {
    return when (gradeLevel) {
        3 -> "Generate exactly 5 multiple-choice math questions for Grade 3 students. Focus on addition, subtraction, simple multiplication, and basic word problems."
        4 -> "Generate exactly 5 multiple-choice math questions for Grade 4 students. Focus on multiplication, division, place value, fractions, and word problems."
        5 -> "Generate exactly 5 multiple-choice math questions for Grade 5 students. Focus on fractions, decimals, multiplication, division, and multi-step word problems."
        else -> "Generate exactly 5 multiple-choice math questions for Grade 3 students."
    }
}

suspend fun fetchInstructionFromBackend(gradeLevel: Int): String {
    return withContext(Dispatchers.IO) {
        val url = URL("http://10.0.2.2:3000/api/instructions/$gradeLevel")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        try {
            val responseCode = connection.responseCode
            Log.d(TAG, "GET instructions -> $responseCode")

            val responseText = readResponseText(connection, responseCode)
            Log.d(TAG, "Instructions raw: $responseText")

            if (responseCode != 200) {
                throw Exception("Instructions request failed: $responseCode")
            }

            val json = JSONObject(responseText)
            json.optString("instructionText", "No instructions found.")
        } finally {
            connection.disconnect()
        }
    }
}

suspend fun fetchGeneratedQuizFromBackend(prompt: String, userId: Int): Pair<List<DisplayQuestion>, Long> {
    return withContext(Dispatchers.IO) {
        val encodedPrompt = URLEncoder.encode(prompt, "UTF-8")
        val url = URL("http://10.0.2.2:3000/api/quiz/generate?prompt=$encodedPrompt&userId=$userId")
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        try {
            val responseCode = connection.responseCode
            Log.d(TAG, "POST quiz/generate -> $responseCode")

            val responseText = readResponseText(connection, responseCode)
            Log.d(TAG, "Quiz raw: $responseText")

            if (responseCode != 200) {
                throw Exception("Quiz generation failed: $responseCode")
            }

            val json = JSONObject(responseText)
            val sessionId = json.optLong("sessionId", -1L)
            val questionsArray = json.optJSONArray("questions") ?: JSONArray()
            Pair(parseQuestions(questionsArray), sessionId)
        } finally {
            connection.disconnect()
        }
    }
}

fun readResponseText(connection: HttpURLConnection, responseCode: Int): String {
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

fun parseQuestions(questionsArray: JSONArray): List<DisplayQuestion> {
    val questions = mutableListOf<DisplayQuestion>()

    for (i in 0 until questionsArray.length()) {
        val questionObject = questionsArray.getJSONObject(i)

        val questionText = questionObject.optString("questionText", "No question text")
        val option1 = questionObject.optString("option1", "")
        val option2 = questionObject.optString("option2", "")
        val option3 = questionObject.optString("option3", "")
        val option4 = questionObject.optString("option4", "")
        val correctAnswer = questionObject.optString("correctAnswer", "N/A")

        val options = listOf(option1, option2, option3, option4)
            .filter { it.isNotBlank() }

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

fun logQuestionsToConsole(questions: List<DisplayQuestion>) {
    if (questions.isEmpty()) {
        Log.d(TAG, "No generated questions returned.")
        return
    }

    questions.forEachIndexed { index, question ->
        Log.d(TAG, "Question ${index + 1}: ${question.questionText}")
        question.options.forEachIndexed { optionIndex, option ->
            val label = ('A' + optionIndex).toString()
            Log.d(TAG, "$label. $option")
        }
        Log.d(TAG, "Correct Answer: ${question.correctAnswer}")
    }
}
