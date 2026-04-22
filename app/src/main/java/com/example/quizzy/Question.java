package com.example.quizzy;

/**
 * Data model representing a single quiz question.
 * It holds the question statement, four possible multiple-choice options,
 * and the designated correct answer.
 */
public class Question {
    private String questionText;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private String correctAnswer;

    /**
     * Constructs a new Question.
     *
     * @param questionText  The text of the question.
     * @param option1       The first answer choice.
     * @param option2       The second answer choice.
     * @param option3       The third answer choice.
     * @param option4       The fourth answer choice.
     * @param correctAnswer The string matching exactly one of the provided options.
     */
    public Question(String questionText, String option1, String option2, String option3, String option4, String correctAnswer) {
        this.questionText = questionText;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.correctAnswer = correctAnswer;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getOption1() {
        return option1;
    }

    public String getOption2() {
        return option2;
    }

    public String getOption3() {
        return option3;
    }

    public String getOption4() {
        return option4;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}
