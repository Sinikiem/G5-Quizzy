package com.quizzy.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizzy.backend.model.QuizResponse;
import com.quizzy.backend.model.QuizSession;
import com.quizzy.backend.repository.QuizSessionRepository;
import org.springframework.stereotype.Service;

@Service
public class QuizService {

    private final AIService aiService;
    private final QuizSessionRepository sessionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QuizService(AIService aiService, QuizSessionRepository sessionRepository) {
        this.aiService = aiService;
        this.sessionRepository = sessionRepository;
    }

    public QuizResponse generateAndStore(String prompt, Integer userId) throws Exception {
        // 1. Llamar a la AI
        String rawJson = aiService.generateQuestionsJson(prompt);

        // 2. Parsear el JSON a QuizResponse
        QuizResponse quizResponse = objectMapper.readValue(rawJson, QuizResponse.class);

        // 3. Guardar en DB (la sesión actual)
        QuizSession session = new QuizSession();
        session.setQuestionsJson(rawJson);
        session.setUserId(userId); // Store the user ID tracking who started the session
        QuizSession saved = sessionRepository.save(session);

        // 4. Incluir el sessionId en la respuesta
        quizResponse.setSessionId(saved.getId());

        return quizResponse;
    }
}
