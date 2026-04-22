package com.example.quizzy.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object NetworkClient {
    private const val BASE_URL = "http://10.0.2.2:3000/api"

    // Suspend version for Kotlin
    suspend fun post(endpoint: String, body: JSONObject): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val result = executePost(endpoint, body)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Synchronous version for Java callers (should be called on background thread)
    @JvmStatic
    fun postSync(endpoint: String, body: JSONObject): JSONObject {
        return executePost(endpoint, body)
    }

    private fun executePost(endpoint: String, body: JSONObject): JSONObject {
        val url = URL("$BASE_URL$endpoint")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        val writer = OutputStreamWriter(connection.outputStream)
        writer.write(body.toString())
        writer.flush()
        writer.close()

        val responseCode = connection.responseCode
        val responseText = readResponse(connection)

        if (responseCode in 200..299) {
            return JSONObject(responseText)
        } else {
            throw Exception("Error $responseCode: $responseText")
        }
    }

    private fun readResponse(connection: HttpURLConnection): String {
        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        } ?: return ""

        val reader = BufferedReader(InputStreamReader(stream))
        return reader.use { it.readText() }
    }
}
