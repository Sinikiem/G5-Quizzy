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

    public String generateStudyPlan(String topic, double accuracy, int gradeLevel) throws Exception {
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

        String gradeContext = gradeLevel == 3
                ? "Grade 3 (age 8-9): focus on basic addition, subtraction, skip counting, simple word problems"
                : gradeLevel == 4
                ? "Grade 4 (age 9-10): focus on multiplication, division, basic fractions, multi-step problems"
                : "Grade 5 (age 10-11): focus on fractions, decimals, percentages, geometry basics";

        String performanceContext = accuracy >= 0.8
                ? "The student is doing well but needs enrichment challenges."
                : accuracy >= 0.5
                ? "The student has partial understanding and needs targeted practice."
                : "The student is struggling and needs foundational review with simple steps.";

        String prompt = """
                You are a friendly math teacher helping a child in %s of the Ontario curriculum.
                %s
                
                The student just completed a quiz on "%s" with %.0f%% accuracy.
                %s
                
                Write a personalized 3-day study plan using simple language a child can understand.
                Activities should be fun, hands-on, and appropriate for their age and performance level.
                Be specific - mention actual math concepts at their grade level, not generic advice.
                
                Return ONLY raw JSON. No markdown. No backticks. No explanation.
                Use this exact format:
                {
                  "topic": "%s",
                  "accuracy": %.0f,
                  "recommendedStudyAreas": [
                    "Specific area 1 for this grade",
                    "Specific area 2 for this grade",
                    "Specific area 3 for this grade"
                  ],
                  "nextSteps": [
                    "Specific fun activity 1 tailored to their score",
                    "Specific fun activity 2 tailored to their score",
                    "Specific fun activity 3 tailored to their score"
                  ],
                  "suggestedResources": [
                    "Kid-friendly resource 1",
                    "Kid-friendly resource 2"
                  ],
                  "estimatedTimeToMastery": "X hours"
                }
                """.formatted(
                gradeContext, performanceContext, topic, accuracy * 100,
                performanceContext, topic, accuracy * 100
        );

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
        while ((line = reader.readLine()) != null) response.append(line);
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