package com.example.quizzy

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.quizzy.network.NetworkClient
import org.json.JSONObject

class StudyPlanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_plan)

        val topic = intent.getStringExtra("topic") ?: "General Math"
        val accuracy = intent.getDoubleExtra("accuracy", 0.5)

        val loadingSpinner = findViewById<ProgressBar>(R.id.studyPlanLoading)
        val contentLayout = findViewById<LinearLayout>(R.id.studyPlanContent)
        val tvTopic = findViewById<TextView>(R.id.tvStudyPlanTopic)
        val tvAccuracy = findViewById<TextView>(R.id.tvStudyPlanAccuracy)
        val tvAreas = findViewById<TextView>(R.id.tvStudyAreas)
        val tvNextSteps = findViewById<TextView>(R.id.tvNextSteps)
        val tvResources = findViewById<TextView>(R.id.tvResources)
        val tvTime = findViewById<TextView>(R.id.tvEstimatedTime)
        val btnBack = findViewById<Button>(R.id.btnStudyPlanBack)
        val tvError = findViewById<TextView>(R.id.tvStudyPlanError)

        btnBack.setOnClickListener { finish() }

        // Show loading, hide content
        loadingSpinner.visibility = View.VISIBLE
        contentLayout.visibility = View.GONE
        tvError.visibility = View.GONE

        Thread {
            try {
                val gradeLevel = intent.getIntExtra("gradeLevel", 3)
                val body = JSONObject().apply {
                    put("topic", topic)
                    put("accuracy", accuracy)
                    put("gradeLevel", gradeLevel)
                }

                val response = NetworkClient.postSync("/study-plan", body)

                runOnUiThread {
                    loadingSpinner.visibility = View.GONE

                    try {
                        tvTopic.text = "Topic: ${response.optString("topic", topic)}"

                        val accuracyPct = (accuracy * 100).toInt()
                        tvAccuracy.text = "$accuracyPct%"

                        // Recommended study areas
                        val areas = response.optJSONArray("recommendedStudyAreas")
                        if (areas != null) {
                            val sb = StringBuilder()
                            for (i in 0 until areas.length()) {
                                sb.append("• ${areas.getString(i)}\n")
                            }
                            tvAreas.text = sb.toString().trim()
                        }

                        // Next steps
                        val steps = response.optJSONArray("nextSteps")
                        if (steps != null) {
                            val sb = StringBuilder()
                            for (i in 0 until steps.length()) {
                                sb.append("${i + 1}. ${steps.getString(i)}\n")
                            }
                            tvNextSteps.text = sb.toString().trim()
                        }

                        // Resources
                        val resources = response.optJSONArray("suggestedResources")
                        if (resources != null) {
                            val sb = StringBuilder()
                            for (i in 0 until resources.length()) {
                                sb.append("• ${resources.getString(i)}\n")
                            }
                            tvResources.text = sb.toString().trim()
                        }

                        // Time to mastery
                        tvTime.text = response.optString("estimatedTimeToMastery", "N/A")
                        contentLayout.visibility = View.VISIBLE

                    } catch (e: Exception) {
                        tvError.text = "Could not read study plan. Please try again."
                        tvError.visibility = View.VISIBLE
                    }
                }

            } catch (e: Exception) {
                runOnUiThread {
                    loadingSpinner.visibility = View.GONE
                    tvError.text = "Failed to connect. Please check your connection."
                    tvError.visibility = View.VISIBLE
                }
            }
        }.start()
    }
}