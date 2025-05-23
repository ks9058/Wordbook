package com.example.wordbook

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

data class EnglishQuizItem(
    val question: String,
    val correctAnswer: String,
    val choices: List<String>
)

class Test_englishActivity : AppCompatActivity() {

    private lateinit var timerText: TextView
    private lateinit var countDownTimer: CountDownTimer

    private lateinit var quizList: List<QuizItem>
    private var currentIndex = 0
    private var correctCount = 0
    private var wrongCount = 0
    private var wordbookNumber = 0

    private var countDownTimerMillis: Long = 10 * 60 * 1000L // 10분

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.test_korea)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.testpage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        wordbookNumber = intent.getIntExtra("wordbook_number", -1)

        timerText = findViewById(R.id.timer)
        startTimer()

        quizList = loadQuizData()
        showQuestion(currentIndex)
    }

    private fun loadQuizData(): List<QuizItem> {
        val quizList = mutableListOf<QuizItem>()
        val db = openOrCreateDatabase("WordbookDB", Context.MODE_PRIVATE, null)

        val cursor = db.rawQuery("SELECT * FROM Word WHERE Book_id = ? ORDER BY RANDOM() LIMIT 20",
            arrayOf(wordbookNumber.toString()))
        while (cursor.moveToNext()) {
            val term = cursor.getString(cursor.getColumnIndexOrThrow("term"))
            val correctDefinition = cursor.getString(cursor.getColumnIndexOrThrow("definition"))

            val wrongCursor = db.rawQuery(
                "SELECT definition FROM Word WHERE Book_id = ? AND definition != ? ORDER BY RANDOM() LIMIT 3",
                arrayOf(wordbookNumber.toString() ,correctDefinition)
            )

            val choices = mutableListOf(correctDefinition)
            while (wrongCursor.moveToNext()) {
                choices.add(wrongCursor.getString(0))
            }
            choices.shuffle()
            wrongCursor.close()

            quizList.add(
                QuizItem(
                    question = term,  // 영어 단어가 문제
                    correctAnswer = correctDefinition,  // 정답은 뜻
                    choices = choices  // 선택지는 뜻들
                )
            )
        }

        cursor.close()
        db.close()
        return quizList
    }

    private fun showQuestion(index: Int) {
        if (index >= quizList.size) {
            moveToResult()
            return
        }

        val item = quizList[index]
        findViewById<TextView>(R.id.test_meaing).text = item.question

        val buttons = listOf<Button>(
            findViewById(R.id.choice_number1),
            findViewById(R.id.choice_number2),
            findViewById(R.id.choice_number3),
            findViewById(R.id.choice_number4)
        )

        for (i in buttons.indices) {
            buttons[i].text = item.choices[i]
            buttons[i].setOnClickListener {
                if (buttons[i].text == item.correctAnswer) {
                    correctCount++
                } else {
                    wrongCount++
                }
                currentIndex++
                showQuestion(currentIndex)
            }
        }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(countDownTimerMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countDownTimerMillis = millisUntilFinished
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                timerText.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                timerText.text = "00:00"
                moveToResult(forceTimeOut = true)
            }
        }.start()
    }

    private fun moveToResult(forceTimeOut: Boolean = false) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("wordbook_number", wordbookNumber)
        intent.putExtra("correctCount", correctCount)
        intent.putExtra("wrongCount", wrongCount)
        intent.putExtra("totalQuestions", quizList.size)
        intent.putExtra("elapsedTime", if (forceTimeOut) 10 * 60 * 1000L else 10 * 60 * 1000L - countDownTimerMillis)
        intent.putExtra("testType", 0) // 영어 시험
        startActivity(intent)
        finish()
    }


    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }
}
