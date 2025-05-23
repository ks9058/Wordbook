package com.example.wordbook

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result)

        val correctCount = intent.getIntExtra("correctCount", 0)
        val wrongCount = intent.getIntExtra("wrongCount", 0)
        val totalQuestions = intent.getIntExtra("totalQuestions", 0)
        val elapsedTime = intent.getLongExtra("elapsedTime", 0L)
        val testType = intent.getIntExtra("testType", 0) // 0: 영어, 1: 한국어
        val bookId = intent.getIntExtra("wordbook_number", 0)

        val minutes = elapsedTime / 1000 / 60
        val seconds = (elapsedTime / 1000) % 60

        val timeText = String.format("걸린시간: %02d:%02d", minutes, seconds)
        findViewById<TextView>(R.id.total_time).text = timeText
        findViewById<TextView>(R.id.total_question).text = totalQuestions.toString()
        findViewById<TextView>(R.id.total_correct_answer).text = correctCount.toString()

        val accuracy = if (totalQuestions > 0) correctCount * 100 / totalQuestions else 0
        val message = when {
            accuracy == 100 -> "완벽해요! 최고에요 😊"
            accuracy >= 80 -> "훌륭해요! 조금만 더 💪"
            accuracy >= 50 -> "괜찮아요! \n 더 노력해봐요 ✨"
            else -> "처음은 누구나 어려워요! \n 다시 도전 🔥"
        }
        findViewById<TextView>(R.id.message_cheerful).text = message

        val retryButton = findViewById<Button>(R.id.retry_btn)
        retryButton.setOnClickListener {
            val retryIntent = when (testType) {
                0 -> Intent(this, Test_englishActivity::class.java)
                1 -> Intent(this, Test_koreaActivity::class.java)
                else -> null
            }
            retryIntent?.let {
                it.putExtra("wordbook_number", bookId)
                startActivity(it)
                finish()
            }
        }

        val backMainButton = findViewById<Button>(R.id.back_main_view_btn)
        backMainButton.setOnClickListener {
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
            finish()
        }

    }
}
