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
import android.widget.Toast
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

class wordListActivity : AppCompatActivity() {
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
        val wordbook_number = intent.getIntExtra("wordbook_number", -1)
        Toast.makeText(this, "$wordbook_number", Toast.LENGTH_SHORT).show()

        //단어장 키를 넘기며 wordView 액티비티 실행
        binding.viewBtn.setOnClickListener {
            val intent = Intent(this, wordViewActivity::class.java)
            intent.putExtra("wordbook_number", wordbook_number)
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

        //임시 데이터

        datas = mutableListOf(
            WordItem(1,"test", "테스트"),
            WordItem(2,"hello", "world"),
            WordItem(3, "asdf", "ㅁㄴㅇㄹ"),
            WordItem(4, "asdf", "ㅁㄴㅇㄹ"),
            WordItem(5, "asdf", "ㅁㄴㅇㄹ"),
            WordItem(6, "asdf", "ㅁㄴㅇㄹ")
        )

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

        //position, 데이터베이서 관련해서 수정하기
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

            //데베관련으로 수정하기
            dialogBinding.confirmBtn.setOnClickListener {
                binding.wordLetter.text = dialogBinding.editLetter.text
                binding.meanLetter.text = dialogBinding.editMean.text

                dialog.dismiss()
            }

        }

        //삭제 텍스트 터치 시 삭제
        binding.deleteText.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                datas.removeAt(pos) //데이터베이스에서 삭제하는 걸로 수정할 것
                notifyItemRemoved(pos)
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