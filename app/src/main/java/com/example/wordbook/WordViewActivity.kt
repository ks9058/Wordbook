package com.example.wordbook

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.wordbook.databinding.WordViewBinding

class WordViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = WordViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var cycleNum = 0
        val currentWord = binding.word
        val currentMean = binding.meaning
        val wordbookNumber = intent.getIntExtra("wordbook_number", -1)
        val db = openOrCreateDatabase("WordbookDB", Context.MODE_PRIVATE, null)
        val cursor = db.rawQuery(
            "SELECT term, definition FROM Word WHERE Book_id = ?",
            arrayOf(wordbookNumber.toString())
        )
        cursor.moveToFirst()

        currentWord.text = cursor.getString(cursor.getColumnIndexOrThrow("term"))
        currentMean.text = cursor.getString(cursor.getColumnIndexOrThrow("definition"))

        binding.nextPage.setOnClickListener {
            cursor.moveToNext()
            if(cursor.isAfterLast) {
                binding.cycleNum.text = "단어장 ${++cycleNum}바퀴 째"
                cursor.moveToFirst()
            }

            currentWord.text = cursor.getString(cursor.getColumnIndexOrThrow("term"))
            currentMean.text = cursor.getString(cursor.getColumnIndexOrThrow("definition"))

        }

        binding.prevPage.setOnClickListener {
            if(cursor.isFirst)
                return@setOnClickListener

            cursor.moveToPrevious()
            currentWord.text = cursor.getString(cursor.getColumnIndexOrThrow("term"))
            currentMean.text = cursor.getString(cursor.getColumnIndexOrThrow("definition"))
        }

        binding.back.setOnClickListener {
            finish()
        }


    }

}