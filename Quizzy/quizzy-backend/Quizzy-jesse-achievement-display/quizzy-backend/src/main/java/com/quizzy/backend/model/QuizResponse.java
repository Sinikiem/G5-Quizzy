package com.quizzy.backend.model;

import java.util.List;

public class QuizResponse {

    private Long sessionId;
    private List<QuestionDto> questions;

    public QuizResponse() {
    }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public List<QuestionDto> getQuestions() {
        return questions;
    }
    public void setQuestions(List<QuestionDto> questions) {
        this.questions = questions;
    }
}