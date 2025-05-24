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


        // 기본 단어장이 존재하는지 확인
        val db = openOrCreateDatabase("WordbookDB", Context.MODE_PRIVATE, null)
        val cursor = db.rawQuery("SELECT COUNT(*) FROM Wordbook", null)
        var hasData = false
        if (cursor.moveToFirst()) {
            hasData = cursor.getInt(0) > 0
        }
        cursor.close()

        if (!hasData) {
            db.execSQL("INSERT INTO Wordbook (title) VALUES ('기본 단어장');")
            val bookId = 1

            val wordList = listOf(
                "apple" to "사과",
                "banana" to "바나나",
                "cat" to "고양이",
                "dog" to "개",
                "elephant" to "코끼리",
                "fish" to "물고기",
                "grape" to "포도",
                "house" to "집",
                "ice" to "얼음",
                "juice" to "주스",
                "kite" to "연",
                "lion" to "사자",
                "monkey" to "원숭이",
                "notebook" to "공책",
                "orange" to "오렌지",
                "pencil" to "연필",
                "queen" to "여왕",
                "rabbit" to "토끼",
                "sun" to "태양",
                "tree" to "나무",
                "umbrella" to "우산",
                "violin" to "바이올린",
                "water" to "물",
                "xylophone" to "실로폰",
                "yogurt" to "요거트",
                "zebra" to "얼룩말",
                "car" to "자동차",
                "book" to "책",
                "flower" to "꽃",
                "moon" to "달"
            )

            val stmt = db.compileStatement("INSERT INTO Word (Book_id, term, definition) VALUES (?, ?, ?)")
            for ((term, definition) in wordList) {
                stmt.bindLong(1, bookId.toLong())
                stmt.bindString(2, term)
                stmt.bindString(3, definition)
                stmt.executeInsert()
            }
        }

        db.close()
    }
}
