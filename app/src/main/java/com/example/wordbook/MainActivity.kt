package com.example.wordbook

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainpage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // DB 파일 경로 확인 및 초기화
        val path: File = getDatabasePath("WordbookDB")
        if (!path.exists()) {
            val db = openOrCreateDatabase("WordbookDB", Context.MODE_PRIVATE, null)

            // 단어장 테이블 생성
            db.execSQL("""
                CREATE TABLE Wordbook (
                    Book_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT
                );
            """.trimIndent())

            // 단어 테이블 생성
            db.execSQL("""
                CREATE TABLE Word (
                    Word_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    Book_id INTEGER,
                    term TEXT,
                    definition TEXT,
                    FOREIGN KEY(Book_id) REFERENCES Wordbook(Book_id)
                );
            """.trimIndent())

            db.close()
        }
        val vocabBtn = findViewById<Button>(R.id.vocabulary_btn)
        val testBtn = findViewById<Button>(R.id.test_btn)

        vocabBtn.setOnClickListener {
            val intent = Intent(this, WordBookActivity::class.java)
            startActivity(intent)
        }

        testBtn.setOnClickListener {
            val intent = Intent(this, TestActivity::class.java)
            startActivity(intent)
        }
    }
}
