package com.example.quizzy;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {

    private TextView tvQuestionNumber, tvQuestion, tvScore, tvFeedback;
    private RadioGroup radioGroup;
    private RadioButton option1, option2, option3, option4;
    private Button btnSubmit, btnNext;
    private ProgressBar progressBar;
    private MaterialCardView feedbackCard;

    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean answered = false;
    private int gradeLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        gradeLevel = getIntent().getIntExtra("GRADE_LEVEL", 3);

        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvScore = findViewById(R.id.tvScore);
        tvFeedback = findViewById(R.id.tvFeedback);
        radioGroup = findViewById(R.id.radioGroup);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnNext = findViewById(R.id.btnNext);
        progressBar = findViewById(R.id.progressBar);
        feedbackCard = findViewById(R.id.feedbackCard);

        questionList = QuizRepository.currentQuizQuestions;

        if (questionList == null || questionList.isEmpty()) {
            Toast.makeText(this, "No questions available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setMax(questionList.size());
        showQuestion();

        btnSubmit.setOnClickListener(v -> validateAnswer());

        btnNext.setOnClickListener(v -> {
            currentQuestionIndex++;
            answered = false;

            if (currentQuestionIndex < questionList.size()) {
                showQuestion();
            } else {
                openResultsScreen();
            }
        });
    }

    private void showQuestion() {
        Question currentQuestion = questionList.get(currentQuestionIndex);

        tvQuestionNumber.setText(String.format(Locale.getDefault(),
                "Question %d of %d",
                currentQuestionIndex + 1,
                questionList.size()));

        tvQuestion.setText(currentQuestion.getQuestionText());
        tvScore.setText(String.format(Locale.getDefault(), "Score: %d", score));

        option1.setText(currentQuestion.getOption1());
        option2.setText(currentQuestion.getOption2());
        option3.setText(currentQuestion.getOption3());
        option4.setText(currentQuestion.getOption4());

        radioGroup.clearCheck();
        enableOptions(true);

        progressBar.setProgress(currentQuestionIndex + 1);
        feedbackCard.setVisibility(View.GONE);

        btnSubmit.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.GONE);
    }

    private void validateAnswer() {
        if (answered) return;

        int selectedId = radioGroup.getCheckedRadioButtonId();

        if (selectedId == -1) {
            View contextView = findViewById(android.R.id.content);
            Snackbar.make(contextView, "Please select an answer", Snackbar.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedButton = findViewById(selectedId);
        String selectedAnswer = selectedButton.getText().toString();
        Question currentQuestion = questionList.get(currentQuestionIndex);

        answered = true;
        enableOptions(false);
        feedbackCard.setVisibility(View.VISIBLE);

        if (selectedAnswer.equals(currentQuestion.getCorrectAnswer())) {
            score++;
            tvFeedback.setText("Correct! 🎉");
            feedbackCard.setCardBackgroundColor(
                    ContextCompat.getColor(this, android.R.color.holo_green_dark)
            );
        } else {
            tvFeedback.setText("Wrong. The correct answer was: " + currentQuestion.getCorrectAnswer());
            feedbackCard.setCardBackgroundColor(
                    ContextCompat.getColor(this, android.R.color.holo_red_dark)
            );
        }

        tvScore.setText(String.format(Locale.getDefault(), "Score: %d", score));
        btnSubmit.setVisibility(View.GONE);
        btnNext.setVisibility(View.VISIBLE);
    }

    private void enableOptions(boolean enable) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(enable);
        }
    }

    private void openResultsScreen() {
        // Logic of badges that are unlocked based on the user's progress.
        BadgeManager.checkAndUnlockBadges(this, score, questionList.size(), gradeLevel);

        Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("totalQuestions", questionList.size());
        startActivity(intent);
        finish();
    }
}
