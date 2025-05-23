package com.example.wordbook

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wordbook.databinding.AddItemBinding
import com.example.wordbook.databinding.GridItemBinding
import com.example.wordbook.databinding.WordBookBinding

class WordBookActivity : AppCompatActivity() {
    // ++데이터 셋++
    val fileList = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        var binding = WordBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //  시작 시 fileList 추가
        val db = openOrCreateDatabase("WordbookDB", Context.MODE_PRIVATE, null)
        val cursor = db.rawQuery("SELECT title FROM Wordbook", null)

        if (cursor.moveToFirst()) {
            do {
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                fileList.add(title)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        // gridLayout Adapter+ click이벤트
        var adapter = CustomAdapter(fileList) { clickedItem ->
            // click이벤트
            val db = openOrCreateDatabase("WordbookDB", Context.MODE_PRIVATE, null)
            val wordIntent:Intent
            val isTest = intent.getIntExtra("testType", 0)

            val cursor = db.rawQuery(
                "SELECT Book_id FROM Wordbook WHERE title = ?",
                arrayOf(clickedItem)
            )
            cursor.moveToFirst()
            val bookId = cursor.getInt(cursor.getColumnIndexOrThrow("Book_id"))

            val cursor2 = db.rawQuery(
                "SELECT COUNT(*) FROM word WHERE Book_id = ?",
                arrayOf(bookId.toString())
            )
            var count = 0
            if (cursor2.moveToFirst()) {
                count = cursor2.getInt(0)
            }

            db.close()
            cursor.close()
            cursor2.close()

            when(isTest) {
                1 -> {
                    if(count == 0) {
                        Toast.makeText(this, "빈 단어장은 선택할 수 없습니다.", Toast.LENGTH_SHORT).show()
                        return@CustomAdapter
                    } else if (count < 4) {
                        Toast.makeText(this, "단어장의 단어가 너무 적습니다.", Toast.LENGTH_SHORT).show()
                        return@CustomAdapter
                    }

                    wordIntent = Intent(this, Test_englishActivity::class.java)
                    wordIntent.putExtra("wordbook_number", bookId)
                    startActivity(wordIntent)
                }
                2 -> {
                    if(count == 0) {
                        Toast.makeText(this, "빈 단어장은 선택할 수 없습니다.", Toast.LENGTH_SHORT).show()
                        return@CustomAdapter
                    } else if (count < 4) {
                        Toast.makeText(this, "단어장의 단어가 너무 적습니다.", Toast.LENGTH_SHORT).show()
                        return@CustomAdapter
                    }

                    wordIntent = Intent(this, Test_koreaActivity::class.java)
                    wordIntent.putExtra("wordbook_number", bookId)
                    startActivity(wordIntent)
                }
                0 -> {
                    wordIntent = Intent(this, WordListActivity::class.java)
                    wordIntent.putExtra("wordbook_number", bookId)
                    startActivity(wordIntent)
                }
            }

        }

        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.adapter = adapter

        // 검색창 자동완성 Adapter
        val autoAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, fileList)
        binding.searchView.setAdapter(autoAdapter)


        // 검색창 텍스트 변경 시의 리스너
        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val filtered = fileList.filter { it.contains(s.toString()) }
                adapter.updateItems(filtered.toMutableList())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 기능 1: 단어추가
        fun showAddItemDialog() {
            val dialogBinding = AddItemBinding.inflate(layoutInflater)
            // 알림창 생성
            val builder: AlertDialog.Builder =  AlertDialog.Builder(this)
            builder.setTitle("폴더 추가")
            builder.setView(dialogBinding.root)
            // 추가버튼 click
            builder.setPositiveButton("추가", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    val db = openOrCreateDatabase("WordbookDB", Context.MODE_PRIVATE, null)
                    val name = dialogBinding.editItemName.text.toString()
                    if(name != "")
                    {
                        val stmt = db.compileStatement("INSERT INTO Wordbook(title) VALUES (?)")
                        stmt.bindString(1, name)
                        try {
                            stmt.executeInsert()
                            fileList.add(name)
                            adapter.updateItems(fileList.toMutableList())
                            Toast.makeText(applicationContext, "${name} 폴더가 추가됨", Toast.LENGTH_SHORT).show()
                        } catch (e: SQLiteConstraintException) {
                            Toast.makeText(applicationContext, "이미 존재하는 이름입니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else
                    {
                        Toast.makeText(applicationContext, "폴더 이름을 입력하세요", Toast.LENGTH_SHORT).show()
                    }
                    db.close()
                }
            })
            // 취소버튼 click
            builder.setNegativeButton("취소", null)
            builder.show()

        }

        // 기능 2: 삭제기능
        fun showDeleteItemDialog() {

            // 빈 폴더일 경우
            if(fileList.isEmpty())
            {
                Toast.makeText(this, "빈 단어장 입니다.", Toast.LENGTH_SHORT).show()
                return
            }

            val items = fileList.toTypedArray()
            val checkedItems = BooleanArray(items.size) { false }

            // 알림창 생성
            val builder: AlertDialog.Builder =  AlertDialog.Builder(this)
            builder.setTitle("삭제할 항목 선택")
            // 다중선택자
            builder.setMultiChoiceItems(items, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }

            builder.setPositiveButton("삭제", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {

                    val db = openOrCreateDatabase("WordbookDB", Context.MODE_PRIVATE, null)
                    val toDelete = mutableListOf<String>()


                    for (i in checkedItems.indices) {
                        if (checkedItems[i]) {
                            val title = fileList[i]

                            val cursor = db.rawQuery(
                                "SELECT Book_id FROM Wordbook WHERE title = ?",
                                arrayOf(title)
                            )
                            cursor.moveToFirst()
                            val bookId = cursor.getInt(cursor.getColumnIndexOrThrow("Book_id"))

                            db.delete("Wordbook", "title = ?", arrayOf(title)) // DB 삭제
                            db.delete("Word", "Book_id = ?", arrayOf(bookId.toString()))
                            toDelete.add(title)                                // 리스트에서도 제거할 목록에 추가
                        }

                    }

                    if (toDelete.isEmpty())
                    {
                        Toast.makeText(applicationContext, "삭제할 항목을 선택하세요", Toast.LENGTH_SHORT).show()
                    }
                    else
                    {
                        fileList.removeAll(toDelete)
                        adapter.updateItems(fileList.toMutableList())
                        Toast.makeText(applicationContext, "${toDelete.size}개 항목 삭제됨", Toast.LENGTH_SHORT).show()
                    }

                    db.close()
                }
            })

            builder.setNegativeButton("취소", null)
            builder.show()
        }



        // 기능 선택 버튼 (기능1, 기능2, ...)
        binding.btnSettings.setOnClickListener {
            fun showFunctionMenu() {
                val options = arrayOf("기능1 - 추가기능", "기능2 - 삭제기능")
                val builder: AlertDialog.Builder =  AlertDialog.Builder(this)
                builder.setTitle("기능 선택")
                builder.setItems(options) { _, which ->
                    when (which) {
                        0 -> showAddItemDialog()    // 추가기능 호출
                        1 -> showDeleteItemDialog()  // 삭제기능 호출
                    }
                }
                builder.show()

            }
            showFunctionMenu()
        }
    }

    // ViewHolder, 이미지+ 텍스트+ click리스너
    class ViewHolder(val binding: GridItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(item: String, onItemClick: (String) -> Unit) {
            binding.itemText.text = item
            binding.itemIcon.setImageResource(R.drawable.file_icon)
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    class CustomAdapter(var items: MutableList<String>, val onItemClick: (String) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            ViewHolder(GridItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val binding = (holder as ViewHolder).binding
            holder.bind(items[position], onItemClick)
        }


        // 데이터 추가 시 업데이트를 알리기
        fun updateItems(newItems: MutableList<String>) {
            items = newItems
            notifyDataSetChanged()
        }


    }
}


