package com.example.wordbook

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result)

        val correctCount = intent.getIntExtra("correctCount", 0)
        val wrongCount = intent.getIntExtra("wrongCount", 0)
        val totalQuestions = intent.getIntExtra("totalQuestions", 0)
        val elapsedTime = intent.getLongExtra("elapsedTime", 0L)

        val minutes = elapsedTime / 1000 / 60
        val seconds = (elapsedTime / 1000) % 60

        Log.d("ResultActivity", "Correct Count: $correctCount")
        Log.d("ResultActivity", "Wrong Count: $wrongCount")
        Log.d("ResultActivity", "Total Questions: $totalQuestions")
        Log.d("ResultActivity", "Elapsed Time: ${minutes}m ${seconds}s")
    }
}
