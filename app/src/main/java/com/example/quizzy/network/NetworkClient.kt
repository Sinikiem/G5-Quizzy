package com.example.quizzy.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * A low-level network client for handling HTTP requests to the backend API.
 * This client supports both coroutine-based asynchronous calls for Kotlin 
 * and synchronous calls for legacy Java code.
 */
object NetworkClient {

    /** The base URL for the backend API, targeting the Android Emulator loopback. */
    private const val BASE_URL = "http://10.0.2.2:3000/api"
    
    private const val TAG = "NetworkClient"
    private const val TIMEOUT_MILLIS = 10000

    /**
     * Executes an asynchronous POST request.
     * 
     * @param endpoint The API endpoint relative to [BASE_URL].
     * @param body     The JSON object to be sent in the request body.
     * @return A [Result] containing the parsed [JSONObject] response or an exception.
     */
    suspend fun post(endpoint: String, body: JSONObject): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val result = executeRequest("POST", endpoint, body)
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "POST failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Executes an asynchronous GET request.
     * 
     * @param endpoint The API endpoint relative to [BASE_URL].
     * @return A [Result] containing the parsed [JSONObject] response or an exception.
     */
    suspend fun get(endpoint: String): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val result = executeRequest("GET", endpoint, null)
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "GET failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Synchronous version of the POST request for Java callers.
     * **Warning:** Must be called from a background thread to avoid NetworkOnMainThreadException.
     * 
     * @param endpoint The API endpoint.
     * @param body     The JSON payload.
     * @return The parsed [JSONObject] response.
     */
    @JvmStatic
    fun postSync(endpoint: String, body: JSONObject): JSONObject {
        return executeRequest("POST", endpoint, body)
    }

    /**
     * Algorithm: Core request execution logic using [HttpURLConnection].
     * 
     * Main steps:
     * 1. Open connection and set headers (Content-Type, Accept).
     * 2. Write body payload for POST/PUT methods.
     * 3. Read response code and stream.
     * 4. Parse the raw response into a JSON object, wrapping arrays if necessary.
     */
    private fun executeRequest(method: String, endpoint: String, body: JSONObject?): JSONObject {
        val url = URL("$BASE_URL$endpoint")

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        connection.connectTimeout = TIMEOUT_MILLIS
        connection.readTimeout = TIMEOUT_MILLIS

        if (body != null && (method == "POST" || method == "PUT")) {
            connection.doOutput = true
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(body.toString())
            }
        }

        val responseCode = connection.responseCode
        val responseText = readResponse(connection)

        if (responseCode in 200..299) {
            return parseJsonResponse(responseText)
        } else {
            throw Exception("Error $responseCode: $responseText")
        }
    }

    /**
     * Parses the raw response string into a JSONObject.
     * Automatically wraps JSONArrays into a JSONObject under the key "data" 
     * for consistent processing.
     */
    private fun parseJsonResponse(responseText: String): JSONObject {
        val trimmed = responseText.trim()

        if (trimmed.isEmpty()) {
            return JSONObject()
        }

        return try {
            JSONObject(trimmed)
        } catch (e: Exception) {
            val wrapper = JSONObject()
            wrapper.put("data", JSONArray(trimmed))
            wrapper
        }
    }

    /**
     * Reads the response stream (either input or error) from the connection.
     */
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
