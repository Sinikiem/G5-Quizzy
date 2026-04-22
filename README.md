Quizzy
An interactive Android application designed to help children in Grades 3–5 practice foundational arithmetic through gamified quiz sessions, AI-generated study plans, and a guardian progress dashboard.

Backend Setup

Navigate to the backend directory:

bashcd quizzy-backend

Add your OpenRouter API key to src/main/resources/application.properties:

propertiesopenrouter.api.key=your_key_here

Start the backend server:

bash./mvnw spring-boot:run
The server runs on http://localhost:3000. Confirm it is running before launching the Android app.

Note: The Android emulator reaches the backend at http://10.0.2.2:3000. This is already configured in NetworkClient.kt and RetrofitClient.java.


Android Setup

Open the project in Android Studio by navigating to:

File → Open → /path/to/Quizzy-main

Wait for Gradle sync to complete.
Ensure the backend is running (see above).
Run the app on an emulator or connected device using the Run button (▶).


Important: Always start the Spring Boot backend before running the Android app. The home screen fetches the student's score on launch and will show a loading state if the backend is unreachable.


AI Study Plan
The Study Plan feature uses the OpenRouter API to generate a personalized 3-day study plan based on the student's quiz topic and accuracy score.
Flow:

Student completes a quiz
Grade level and accuracy are passed to ResultActivity
Student or guardian taps Generate Study Plan
Android sends a POST /api/study-plan request to the backend with { topic, accuracy, gradeLevel }
The backend's AIService constructs a grade-appropriate prompt and calls OpenRouter (GPT-3.5-turbo)
The AI returns a structured JSON study plan
Android parses and displays the plan in StudyPlanActivity

The prompt is tailored by grade level:

Grade 3 (Addition, subtraction, skip counting, simple word problems)
Grade 4 (Multiplication, division, basic fractions, multi-step problems)
Grade 5 (Fractions, decimals, percentages, geometry basics)

The plan also adapts to the student's performance level. Students scoring below 50% receive foundational review activities, while higher-scoring students receive enrichment challenges.

Team

Sinikiem Azaiki : Android Development, AI Study Plan Integration
Adejuwon Abiola : Android Development, Achievement System
Burak Koseoglu  : Backend Development, Spring Boot API
Amirhossein Mohammadi : Backend Development, Database & Score System
Khoi Nguyen Phan : Android Development, Guardian Dashboard