package com.example.wordbook

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wordbook.databinding.WordLayoutBinding
import com.example.wordbook.databinding.WordListBinding

class wordListActivity : AppCompatActivity() {
    lateinit var adapter: MyAdapter

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

        binding.viewBtn.setOnClickListener {
            val intent = Intent(this, wordViewActivity::class.java)
            startActivity(intent)
        }

        binding.searchBarImage.setOnClickListener {
            binding.search.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.search, InputMethodManager.SHOW_IMPLICIT)
        }


        val datas = mutableListOf(
            WordItem("test", "테스트"),
            WordItem("test2", "테스트2")
        )

        adapter = MyAdapter(datas)
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

    }

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
    val letter: String,
    val mean: String
)

class MyViewHolder(val binding:WordLayoutBinding): RecyclerView.ViewHolder(binding.root)

class MyAdapter(val datas: MutableList<WordItem>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var editContainerOpened: View? = null

    override fun getItemCount(): Int = datas.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        MyViewHolder(WordLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as MyViewHolder).binding

        binding.wordLetter.text = datas[position].letter
        binding.meanLetter.text = datas[position].mean

        binding.deleteText.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                datas.removeAt(pos)
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

}