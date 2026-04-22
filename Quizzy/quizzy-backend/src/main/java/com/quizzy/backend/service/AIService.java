package com.quizzy.backend.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class AIService {

    @Value("${openrouter.api.key}")
    private String apiKey;

    public String generateQuestionsJson(String prompt) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("OpenRouter API key is missing. Check your environment variable and application.properties.");
        }

        URL url = new URL("https://openrouter.ai/api/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("HTTP-Referer", "http://localhost:3000");
        conn.setRequestProperty("X-Title", "Quizzy Backend");
        conn.setDoOutput(true);

        String fullPrompt = """
                Generate exactly 5 multiple-choice math questions.
                Return ONLY raw JSON.
                Do not use markdown.
                Do not use triple backticks.
                Do not add explanations.
                Use this exact format:
                {
                  "questions": [
                    {
                      "questionText": "What is 2 + 2?",
                      "option1": "3",
                      "option2": "4",
                      "option3": "5",
                      "option4": "6",
                      "correctAnswer": "4"
                    }
                  ]
                }

                Prompt instructions:
                %s
                """.formatted(prompt);

        JSONObject body = new JSONObject();
        body.put("model", "openai/gpt-3.5-turbo");

        JSONArray messages = new JSONArray();
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", fullPrompt);
        messages.put(userMessage);

        body.put("messages", messages);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream(),
                        StandardCharsets.UTF_8
                )
        );

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        if (code < 200 || code >= 300) {
            throw new RuntimeException("OpenRouter error: " + response);
        }

        JSONObject json = new JSONObject(response.toString());

        String content = json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim();

        if (content.startsWith("```json")) {
            content = content.substring(7).trim();
        }
        if (content.startsWith("```")) {
            content = content.substring(3).trim();
        }
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3).trim();
        }

        return content;
    }

    public String generateStudyPlan(String topic, double accuracy) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("OpenRouter API key is missing.");
        }

        URL url = new URL("https://openrouter.ai/api/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("HTTP-Referer", "http://localhost:3000");
        conn.setRequestProperty("X-Title", "Quizzy Backend");
        conn.setDoOutput(true);

        String prompt = """
                A student in grades 3-5 recently completed a math quiz on the topic: "%s".
                Their accuracy was %.0f%%.
                Generate a simple 3-day study plan to help them improve.
                Return ONLY raw JSON. No markdown. No backticks. No explanation.
                Use this exact format:
                {
                  "topic": "%s",
                  "accuracy": %.0f,
                  "days": [
                    {
                      "day": 1,
                      "title": "Review Basics",
                      "activities": ["Activity 1", "Activity 2", "Activity 3"]
                    },
                    {
                      "day": 2,
                      "title": "Practice",
                      "activities": ["Activity 1", "Activity 2", "Activity 3"]
                    },
                    {
                      "day": 3,
                      "title": "Challenge",
                      "activities": ["Activity 1", "Activity 2", "Activity 3"]
                    }
                  ]
                }
                """.formatted(topic, accuracy * 100, topic, accuracy * 100);

        JSONObject body = new JSONObject();
        body.put("model", "openai/gpt-3.5-turbo");

        JSONArray messages = new JSONArray();
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.put(userMessage);

        body.put("messages", messages);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream(),
                        StandardCharsets.UTF_8
                )
        );

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        if (code < 200 || code >= 300) {
            throw new RuntimeException("OpenRouter error: " + response);
        }

        JSONObject json = new JSONObject(response.toString());

        String content = json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim();

        if (content.startsWith("```json")) content = content.substring(7).trim();
        if (content.startsWith("```")) content = content.substring(3).trim();
        if (content.endsWith("```")) content = content.substring(0, content.length() - 3).trim();

        return content;
    }
}