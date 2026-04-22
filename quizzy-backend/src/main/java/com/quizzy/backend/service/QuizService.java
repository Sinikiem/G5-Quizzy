package com.quizzy.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizzy.backend.model.QuestionDto;
import com.quizzy.backend.model.QuizResponse;
import com.quizzy.backend.model.QuizSession;
import com.quizzy.backend.repository.QuizSessionRepository;
import org.springframework.stereotype.Service;

/**
 * Service responsible for orchestrating quiz generation via AI and persisting session data.
 */
@Service
public class QuizService {

    private final AIService aiService;
    private final QuizSessionRepository sessionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QuizService(AIService aiService, QuizSessionRepository sessionRepository) {
        this.aiService = aiService;
        this.sessionRepository = sessionRepository;
    }

    /**
     * Algorithm: AI Quiz Generation and Persistence.
     * 1. Calls AI service to generate a JSON string of questions.
     * 2. Deserializes the JSON into a QuizResponse model.
     * 3. Logs the selected grade level and the complete quiz details to the backend console.
     * 4. Persists the raw session data and user association to the database.
     * 5. Returns the response augmented with the new session ID.
     *
     * @param prompt natural language instructions for question type/difficulty.
     * @param userId association with the student user.
     * @param level  the selected grade level.
     * @return a hydrated [QuizResponse] containing generated questions.
     */
    public QuizResponse generateAndStore(String prompt, Integer userId, Integer level) throws Exception {
        // 1. Trigger AI Generation
        String rawJson = aiService.generateQuestionsJson(prompt);

        // 2. Parse JSON to Response Object
        QuizResponse quizResponse = objectMapper.readValue(rawJson, QuizResponse.class);

        // 3. Log Details to Backend Console (Single Source of Truth for logging)
        logQuizDetailsToConsole(quizResponse, level);

        // 4. Persist Session to Database
        QuizSession session = new QuizSession();
        session.setQuestionsJson(rawJson);
        session.setUserId(userId);
        QuizSession saved = sessionRepository.save(session);

        // 5. Hydrate response with Session ID
        quizResponse.setSessionId(saved.getId());

        return quizResponse;
    }

    /**
     * Formats and prints the quiz metadata and questions to the system console.
     */
    private void logQuizDetailsToConsole(QuizResponse quizResponse, Integer level) {
        System.out.println("\n--- GENERATING NEW QUIZ ---");
        System.out.println("Grade Level: " + (level != null ? level : "Not Specified"));
        System.out.println("================================");

        if (quizResponse.getQuestions() != null) {
            for (int i = 0; i < quizResponse.getQuestions().size(); i++) {
                QuestionDto q = quizResponse.getQuestions().get(i);
                System.out.println("Question " + (i + 1) + ": " + q.getQuestionText());
                System.out.println("  1) " + q.getOption1());
                System.out.println("  2) " + q.getOption2());
                System.out.println("  3) " + q.getOption3());
                System.out.println("  4) " + q.getOption4());
                System.out.println("  [CORRECT]: " + q.getCorrectAnswer());
                System.out.println();
            }
        }
        System.out.println("================================\n");
    }
}
