package com.example.wordbook

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
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
    lateinit var dbHelper: WordbookDbHelper
    lateinit var db: SQLiteDatabase

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

        dbHelper = WordbookDbHelper(this)
        db = dbHelper.writableDatabase

        fileList.clear()
        val cursor = db.rawQuery("SELECT title FROM Wordbook", null)
        while (cursor.moveToNext()) {
            val title = cursor.getString(0)
            fileList.add(title)
        }
        cursor.close()


        // gridLayout Adapter+ click이벤트
        var adapter = CustomAdapter(fileList) { clickedItem ->
            // click이벤트

            val wordIntent = Intent(this, wordListActivity::class.java)
            //데이터베이스 구축까지 임시 value 값, value에 Book_id 넘기기
            wordIntent.putExtra("wordbook_number", 123456789)
            startActivity(wordIntent)

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
                Toast.makeText(this, "빈 폴더 입니다.", Toast.LENGTH_SHORT).show()
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
                    val toDelete = mutableListOf<String>()

                    for (i in checkedItems.indices) {
                        if (checkedItems[i]) {
                            val title = fileList[i]
                            db.delete("Wordbook", "title = ?", arrayOf(title)) // DB 삭제
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

class WordbookDbHelper(context: Context) : SQLiteOpenHelper(context, "wordbook.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS Wordbook (
                Book_id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT UNIQUE
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS Word (
                Word_id INTEGER PRIMARY KEY AUTOINCREMENT,
                Book_id INTEGER,
                term TEXT,
                definition TEXT,
                FOREIGN KEY(Book_id) REFERENCES Wordbook(Book_id)
            );
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Word")
        db.execSQL("DROP TABLE IF EXISTS Wordbook")
        onCreate(db)
    }
}
