package com.example.quizzy

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.content.ContextCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.card.MaterialCardView
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuizFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        // Clear any previous state in the repository to ensure test isolation
        QuizRepository.currentQuizQuestions = mutableListOf()
    }

    // --- Dashboard and Navigation (7 Tests) ---

    @Test
    fun testMainDashboardLoadsOnLaunch() {
        // Test 1: Verify Dashboard loads with Logo, Play button and Nav Bar
        composeTestRule.onNodeWithText("Q").assertIsDisplayed()
        composeTestRule.onNodeWithText("uizzy").assertIsDisplayed()
        composeTestRule.onNodeWithText("PLAY").assertIsDisplayed()
        
        // Check for navigation icons by content description
        composeTestRule.onNodeWithContentDescription("Home").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Awards").assertIsDisplayed()
    }

    @Test
    fun testMainDashboardIsDefaultLandingScreen() {
        // Test 2: Launching shows Dashboard as default
        composeTestRule.onNodeWithText("Ready to test your knowledge?").assertIsDisplayed()
        composeTestRule.onNodeWithText("PLAY").assertIsDisplayed()
    }

    @Test
    fun testStartQuizButtonOpensSelectionScreen() {
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.onNodeWithText("Select Grade").assertIsDisplayed()
    }

    @Test
    fun testSelectionScreenDisplaysGradeOptions() {
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.onNodeWithText("Grade 3").assertIsDisplayed()
        composeTestRule.onNodeWithText("Grade 4").assertIsDisplayed()
        composeTestRule.onNodeWithText("Grade 5").assertIsDisplayed()
    }

    @Test
    fun testNavigationBackToHome() {
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.onNodeWithText("Home").performClick()
        composeTestRule.onNodeWithText("Ready to test your knowledge?").assertIsDisplayed()
    }

    @Test
    fun testSelectionScreenLoadsCorrectly() {
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.onNodeWithText("Select Grade").assertIsDisplayed()
        composeTestRule.onNodeWithText("Choose your level to begin the quiz").assertIsDisplayed()
    }

    @Test
    fun testGradeOptionsAreClearlyVisible() {
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.onNodeWithText("Grade 3").assertIsDisplayed()
        composeTestRule.onNodeWithText("Grade 4").assertIsDisplayed()
        composeTestRule.onNodeWithText("Grade 5").assertIsDisplayed()
    }

    @Test
    fun testGradeOptionsAreSelectable() {
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.onNodeWithText("Grade 3").assertHasClickAction()
        composeTestRule.onNodeWithText("Grade 4").assertHasClickAction()
        composeTestRule.onNodeWithText("Grade 5").assertHasClickAction()
    }

    // --- Selection and Error State (4 Tests) ---

    @Test
    fun testSelectGrade3StartsQuiz() {
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.onNodeWithText("Grade 3").performClick()
        waitForView(withId(R.id.tvQuestionNumber), timeout = 30000)
        onView(withId(R.id.tvQuestionNumber)).check(matches(withText("Question 1 of 5")))
    }

    @Test
    fun testSelectGrade4StartsQuiz() {
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.onNodeWithText("Grade 4").performClick()
        waitForView(withId(R.id.tvQuestionNumber), timeout = 30000)
        onView(withId(R.id.tvQuestionNumber)).check(matches(withText("Question 1 of 5")))
    }

    @Test
    fun testSelectGrade5StartsQuiz() {
        composeTestRule.onNodeWithText("PLAY").performClick()
        composeTestRule.onNodeWithText("Grade 5").performClick()
        waitForView(withId(R.id.tvQuestionNumber), timeout = 30000)
        onView(withId(R.id.tvQuestionNumber)).check(matches(withText("Question 1 of 5")))
    }

    @Test
    fun testBackendErrorShowsErrorState() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), InstructionsActivity::class.java).apply {
            putExtra("GRADE_LEVEL", 999)
        }
        ActivityScenario.launch<InstructionsActivity>(intent)

        composeTestRule.waitUntil(timeoutMillis = 15000) {
            composeTestRule.onAllNodesWithText("failed", substring = true).fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("wrong", substring = true).fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("No instructions", substring = true).fetchSemanticsNodes().isNotEmpty()
        }

        try {
            composeTestRule.onNodeWithText("failed", substring = true).assertIsDisplayed()
        } catch (e: AssertionError) {
            try {
                composeTestRule.onNodeWithText("No instructions", substring = true).assertIsDisplayed()
            } catch (e2: AssertionError) {
                composeTestRule.onNodeWithText("wrong", substring = true).assertIsDisplayed()
            }
        }
    }

    // --- Question Rendering and Options (6 Tests) ---

    @Test
    fun testFirstQuestionLoadsCorrectly() {
        val mockQuestion = Question("What is 5 + 5?", "8", "9", "10", "11", "10")
        QuizRepository.currentQuizQuestions = listOf(mockQuestion)
        ActivityScenario.launch(QuizActivity::class.java)

        onView(withId(R.id.tvQuestion)).check(matches(withText("What is 5 + 5?")))
    }

    @Test
    fun testQuestionIsReadable() {
        val mockQuestion = Question("Is this question readable?", "Yes", "No", "Maybe", "Sure", "Yes")
        QuizRepository.currentQuizQuestions = listOf(mockQuestion)
        ActivityScenario.launch(QuizActivity::class.java)

        onView(withId(R.id.tvQuestion)).check(matches(isDisplayed()))
    }

    @Test
    fun testExactlyFourOptionsAreDisplayed() {
        val mockQuestion = Question("Test Options", "Opt1", "Opt2", "Opt3", "Opt4", "Opt1")
        QuizRepository.currentQuizQuestions = listOf(mockQuestion)
        ActivityScenario.launch(QuizActivity::class.java)

        onView(withId(R.id.radioGroup)).check(matches(hasChildCount(4)))
    }

    @Test
    fun testOneCorrectThreeIncorrectOptions() {
        val mockQuestion = Question("Logic check?", "Correct", "Wrong1", "Wrong2", "Wrong3", "Correct")
        QuizRepository.currentQuizQuestions = listOf(mockQuestion)
        ActivityScenario.launch(QuizActivity::class.java)

        onView(withText("Correct")).check(matches(isDisplayed()))
        onView(withText("Wrong1")).check(matches(isDisplayed()))
        onView(withText("Wrong2")).check(matches(isDisplayed()))
        onView(withText("Wrong3")).check(matches(isDisplayed()))
    }

    @Test
    fun testOptionCanBeSelected() {
        val mockQuestion = Question("Tap me", "A", "B", "C", "D", "A")
        QuizRepository.currentQuizQuestions = listOf(mockQuestion)
        ActivityScenario.launch(QuizActivity::class.java)

        onView(withId(R.id.option2)).perform(click())
        onView(withId(R.id.option2)).check(matches(isChecked()))
    }

    @Test
    fun testAllElementsAreClearlyVisible() {
        val mockQuestion = Question("Visibility Test", "1", "2", "3", "4", "1")
        QuizRepository.currentQuizQuestions = listOf(mockQuestion)
        ActivityScenario.launch(QuizActivity::class.java)

        onView(withId(R.id.tvQuestion)).check(matches(isDisplayed()))
        onView(withId(R.id.option1)).check(matches(isDisplayed()))
        onView(withId(R.id.option2)).check(matches(isDisplayed()))
        onView(withId(R.id.option3)).check(matches(isDisplayed()))
        onView(withId(R.id.option4)).check(matches(isDisplayed()))
    }

    // --- Evaluation and Scoring (3 Tests) ---

    @Test
    fun testSystemEvaluatesAnswerOnSubmission() {
        val mockQuestion = Question("1+1?", "1", "2", "3", "4", "2")
        QuizRepository.currentQuizQuestions = listOf(mockQuestion)
        ActivityScenario.launch(QuizActivity::class.java)

        onView(withText("2")).perform(click())
        onView(withId(R.id.btnSubmit)).perform(click())

        onView(withId(R.id.feedbackCard)).check(matches(isDisplayed()))
    }

    @Test
    fun testScoreUpdatesOnCorrectSubmission() {
        val mockQuestion = Question("1+1?", "1", "2", "3", "4", "2")
        QuizRepository.currentQuizQuestions = listOf(mockQuestion)
        ActivityScenario.launch(QuizActivity::class.java)

        onView(withText("2")).perform(click())
        onView(withId(R.id.btnSubmit)).perform(click())

        onView(withId(R.id.tvScore)).check(matches(withText("Score: 1")))
    }

    @Test
    fun testNoEvaluationWithoutSelection() {
        val mockQuestion = Question("1+1?", "1", "2", "3", "4", "2")
        QuizRepository.currentQuizQuestions = listOf(mockQuestion)
        ActivityScenario.launch(QuizActivity::class.java)

        onView(withId(R.id.btnSubmit)).perform(click())
        onView(withId(R.id.feedbackCard)).check(matches(not(isDisplayed())))
    }

    // --- Feedback and Polish (5 Tests) ---

    @Test
    fun testPositiveFeedbackIsGreen() {
        val mockQuestion = Question("1+1?", "1", "2", "3", "4", "2")
        QuizRepository.currentQuizQuestions = listOf(mockQuestion)
        ActivityScenario.launch(QuizActivity::class.java)

        onView(withText("2")).perform(click())
        onView(withId(R.id.btnSubmit)).perform(click())

        onView(withId(R.id.feedbackCard)).check(matches(hasCardBackgroundColor(android.R.color.holo_green_dark)))
    }

    @Test
    fun testNegativeFeedbackIsRed() {
        val mockQuestion = Question("1+1?", "1", "2", "3", "4", "2")
        QuizRepository.currentQuizQuestions = listOf(mockQuestion)
        ActivityScenario.launch(QuizActivity::class.java)

        onView(withText("1")).perform(click())
        onView(withId(R.id.btnSubmit)).perform(click())

        onView(withId(R.id.feedbackCard)).check(matches(hasCardBackgroundColor(android.R.color.holo_red_dark)))
    }

    @Test
    fun testNegativeFeedbackShowsCorrectAnswer() {
        val mockQuestion = Question("1+1?", "1", "2", "3", "4", "2")
        QuizRepository.currentQuizQuestions = listOf(mockQuestion)
        ActivityScenario.launch(QuizActivity::class.java)

        onView(withText("1")).perform(click())
        onView(withId(R.id.btnSubmit)).perform(click())

        onView(withId(R.id.tvFeedback)).check(matches(withText(containsString("correct answer was: 2"))))
    }

    @Test
    fun testFeedbackAppearsImmediately() {
        val mockQuestion = Question("1+1?", "1", "2", "3", "4", "2")
        QuizRepository.currentQuizQuestions = listOf(mockQuestion)
        ActivityScenario.launch(QuizActivity::class.java)

        onView(withText("2")).perform(click())
        onView(withId(R.id.btnSubmit)).perform(click())

        onView(withId(R.id.feedbackCard)).check(matches(isDisplayed()))
    }

    @Test
    fun testScoreUpdatesCorrectlyAcrossMultipleQuestions() {
        val q1 = Question("1+1?", "1", "2", "3", "4", "2")
        val q2 = Question("2+2?", "3", "4", "5", "6", "4")
        QuizRepository.currentQuizQuestions = listOf(q1, q2)
        ActivityScenario.launch(QuizActivity::class.java)

        onView(withText("2")).perform(click())
        onView(withId(R.id.btnSubmit)).perform(click())
        onView(withId(R.id.btnNext)).perform(click())
        onView(withText("4")).perform(click())
        onView(withId(R.id.btnSubmit)).perform(click())

        onView(withId(R.id.tvScore)).check(matches(withText("Score: 2")))
    }

    // --- Helpers ---

    private fun waitForView(matcher: Matcher<View>, timeout: Long = 10000) {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + timeout

        while (System.currentTimeMillis() < endTime) {
            try {
                onView(matcher).check(matches(isDisplayed()))
                return
            } catch (e: Exception) {
                Thread.sleep(500)
            }
        }
        onView(matcher).check(matches(isDisplayed()))
    }

    private fun hasChildCount(count: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("has child count: $count")
            }
            override fun matchesSafely(view: View): Boolean {
                return view is ViewGroup && view.childCount == count
            }
        }
    }

    private fun hasCardBackgroundColor(colorRes: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("has card background color resource: $colorRes")
            }
            override fun matchesSafely(view: View): Boolean {
                if (view !is MaterialCardView) return false
                val expectedColor = ContextCompat.getColor(view.context, colorRes)
                return view.cardBackgroundColor.defaultColor == expectedColor
            }
        }
    }
}
