package com.example.wordbook

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.test)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.testpage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val meaning_test_btn = findViewById<Button>(R.id.meaning_test_btn)
        val word_test_btn = findViewById<Button>(R.id.word_test_btn)

        word_test_btn.setOnClickListener {
            val intent = Intent(this, WordBookActivity::class.java)
            intent.putExtra("testType", 1)  // 1: 단어 맞추기
            startActivity(intent)
        }

        meaning_test_btn.setOnClickListener {
            val intent = Intent(this, WordBookActivity::class.java)
            intent.putExtra("testType", 2)  // 2: 뜻 맞추기
            startActivity(intent)
        }
    }
}
