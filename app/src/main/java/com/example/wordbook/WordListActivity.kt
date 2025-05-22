package com.example.wordbook

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wordbook.databinding.EditDialogLayoutBinding
import com.example.wordbook.databinding.WordLayoutBinding
import com.example.wordbook.databinding.WordListBinding

class WordListActivity : AppCompatActivity() {
    lateinit var adapter: MyAdapter
    lateinit var datas: MutableList<WordItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = WordListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //액티비티를 실행할 때 단어장 키를 받아옴
        val wordbookNumber = intent.getIntExtra("wordbook_number", -1)

        datas = mutableListOf()

        if (wordbookNumber != -1) {
            val db = openOrCreateDatabase("WordbookDB", Context.MODE_PRIVATE, null)

            val cursor = db.rawQuery(
                "SELECT Word_id, term, definition FROM Word WHERE Book_id = ?",
                arrayOf(wordbookNumber.toString())
            )

            if (cursor.moveToFirst()) {
                do {
                    val index = cursor.getInt(cursor.getColumnIndexOrThrow("Word_id"))
                    val letter = cursor.getString(cursor.getColumnIndexOrThrow("term"))
                    val mean = cursor.getString(cursor.getColumnIndexOrThrow("definition"))

                    datas.add(WordItem(wordbookNumber, index, letter, mean))
                } while (cursor.moveToNext())
            }

            cursor.close()
            db.close()
        }

        //단어장 키를 넘기며 wordView 실행
        binding.viewBtn.setOnClickListener {
            val intent = Intent(this, WordViewActivity::class.java)
            intent.putExtra("wordbook_number", wordbookNumber)
            startActivity(intent)
        }

        //검색창 근처 눌렀을때 검색창 활성화
        binding.searchBarImage.setOnClickListener {
            binding.search.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.search, InputMethodManager.SHOW_IMPLICIT)
        }

        //검색창 검색
        binding.search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                adapter.filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        //단어 추가 버튼
        binding.bottomBtn.setOnClickListener {
            val dialogBinding = EditDialogLayoutBinding.inflate(layoutInflater)

            dialogBinding.titleText.text = "추가"
            dialogBinding.editLetter.setText("")
            dialogBinding.editLetter.hint = "단어를 입력하세요."
            dialogBinding.editMean.setText("")
            dialogBinding.editMean.hint = "뜻을 입력하세요."

            val dialog = AlertDialog.Builder(this)
                .setView(dialogBinding.root)
                .create()

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()

            dialogBinding.cancelBtn.setOnClickListener {
                dialog.dismiss()
            }

            dialogBinding.confirmBtn.setOnClickListener {

                if(dialogBinding.editLetter.text.toString() == "" || dialogBinding.editMean.text.toString() == "") {
                    if(dialogBinding.editLetter.text.toString() == "") {
                        dialogBinding.editLetter.hint = "빈칸을 채워주세요."
                    }
                    if(dialogBinding.editMean.text.toString() == "") {
                        dialogBinding.editMean.hint = "빈칸을 채워주세요."
                    }
                    return@setOnClickListener
                }

                val db = openOrCreateDatabase("WordbookDB", Context.MODE_PRIVATE, null)

                db.execSQL(
                    "INSERT INTO Word (Book_id, term, definition) VALUES (?, ?, ?)",
                    arrayOf(wordbookNumber.toString(), dialogBinding.editLetter.text.toString(), dialogBinding.editMean.text.toString())
                )

                val cursor = db.rawQuery("SELECT last_insert_rowid()", null)
                var word_index = -1
                if (cursor.moveToFirst()) {
                    word_index = cursor.getInt(0)
                }
                cursor.close()
                db.close()

                val newItem = WordItem(
                    book_id = wordbookNumber,
                    index = word_index,
                    letter = dialogBinding.editLetter.text.toString(),
                    mean = dialogBinding.editMean.text.toString()
                )
                datas.add(newItem)
                adapter.notifyDataSetChanged()

                dialog.dismiss()
            }

        }


        adapter = MyAdapter(datas)
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

    }

    //임의의 화면 터치시, 단어 편집창 제거, 키보드 제거
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            adapter.closeEditContainer()
        }
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

        return super.dispatchTouchEvent(ev)
    }
}

data class WordItem(
    val book_id: Int,
    val index: Int,
    val letter: String,
    val mean: String
)

class MyViewHolder(val binding:WordLayoutBinding): RecyclerView.ViewHolder(binding.root)

class MyAdapter(val datas: MutableList<WordItem>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var editContainerOpened: View? = null
    private val originalList = datas.toList()

    override fun getItemCount(): Int = datas.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        MyViewHolder(WordLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as MyViewHolder).binding

        binding.wordLetter.text = datas[position].letter
        binding.meanLetter.text = datas[position].mean

        //편집 텍스트 터치 시 수정
        binding.editText.setOnClickListener {
            val context = holder.itemView.context
            val dialogBinding = EditDialogLayoutBinding.inflate(LayoutInflater.from(context))

            dialogBinding.editLetter.setText(datas[position].letter)
            dialogBinding.editMean.setText(datas[position].mean)

            val dialog = AlertDialog.Builder(context)
                .setView(dialogBinding.root)
                .create()

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()

            dialogBinding.cancelBtn.setOnClickListener {
                dialog.dismiss()
            }

            dialogBinding.confirmBtn.setOnClickListener {
                val wordbook_id = datas[position].book_id
                val newLetter = dialogBinding.editLetter.text.toString()
                val newMean = dialogBinding.editMean.text.toString()
                val wordId = datas[position].index

                if(newLetter == "" || newMean == "") {
                    if(newLetter == "") {
                        dialogBinding.editLetter.hint = "빈칸을 채워주세요."
                    }
                    if(newMean == "") {
                        dialogBinding.editMean.hint = "빈칸을 채워주세요."
                    }
                    return@setOnClickListener
                }

                // DB 수정
                val db = context.openOrCreateDatabase("WordbookDB", Context.MODE_PRIVATE, null)
                db.execSQL(
                    "UPDATE Word SET term = ?, definition = ? WHERE Word_id = ? AND Book_id = ?",
                    arrayOf(newLetter, newMean, wordId.toString(), wordbook_id.toString())
                )
                db.close()

                //datas 수정
                datas[position] = WordItem(wordbook_id, wordId, newLetter, newMean)

                //RecyclerView 갱신
                notifyItemChanged(position)

                //종료
                dialog.dismiss()
            }

        }

        //삭제 텍스트 터치 시 삭제
        binding.deleteText.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val wordbook_id = datas[position].book_id
                val wordId = datas[position].index
                val context = holder.itemView.context

                val db = context.openOrCreateDatabase("WordbookDB", Context.MODE_PRIVATE, null)
                db.execSQL(
                    "DELETE FROM Word WHERE Word_id = ? AND Book_id = ?",
                    arrayOf(wordId.toString(), wordbook_id.toString())
                )
                db.close()

                datas.removeAt(position)
                notifyItemRemoved(position)
            }
        }

        binding.wordContainer.setOnLongClickListener {
            val container = binding.editContainer

            if (container.visibility == View.GONE) {
                editContainerOpened?.visibility = View.GONE
                container.visibility = View.VISIBLE
                editContainerOpened = container
                container.alpha = 0f
                container.translationY = -20f
                container.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start()
            } else {
                container.animate()
                    .alpha(0f)
                    .translationY(-10f)
                    .scaleY(0f)
                    .setDuration(100)
                    .withEndAction {
                        container.visibility = View.GONE
                    }
                    .start()
            }
            true
        }

        binding.wordContainer.setOnClickListener {
            val container = binding.editContainer

            if (container.visibility == View.VISIBLE)
                container.animate()
                    .alpha(0f)
                    .translationY(-10f)
                    .scaleY(0f)
                    .setDuration(100)
                    .withEndAction {
                        container.visibility = View.GONE
                    }
                    .start()
        }
    }

    fun closeEditContainer() {
        val container = editContainerOpened

        container?.animate()
            ?.alpha(0f)
            ?.translationY(-10f)
            ?.scaleY(0f)
            ?.setDuration(100)
            ?.withEndAction {
                container?.visibility = View.GONE
            }
            ?.start()
    }

    fun filter(query: String) {
        val filtered = if (query.isBlank()) {
            originalList
        } else {
            originalList.filter {
                it.letter.contains(query, ignoreCase = true) || it.mean.contains(query, ignoreCase = true)
            }
        }

        datas.clear()
        datas.addAll(filtered)
        notifyDataSetChanged()
    }

}