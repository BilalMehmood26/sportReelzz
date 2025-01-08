package com.usaclean.sportreelzz.ui.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.usaclean.sportreelzz.databinding.ActivityTestBinding
import java.util.UUID

class TestActivity : AppCompatActivity() {

    private lateinit var adapter: TestCommentAdapter
    private var commentsList: ArrayList<TestComments>? = ArrayList()

    private var _binding: ActivityTestBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setAdapter()
        commentsList?.add(TestComments("1", "This is a comment", "User1", System.currentTimeMillis()))
        commentsList?.add(TestComments("2", "Another comment", "User2", System.currentTimeMillis()))
        adapter.notifyDataSetChanged()

    }

    private fun showReplyInput(parentComment: TestComments) {
        val editText = EditText(this).apply {
            hint = "Write a reply..."
        }

        AlertDialog.Builder(this)
            .setTitle("Reply to ${parentComment.toId}")
            .setView(editText)
            .setPositiveButton("Reply") { _, _ ->
                val replyText = editText.text.toString()
                if (replyText.isNotEmpty()) {
                     parentComment.reply?.add(
                         TestComments(
                             commentId = UUID.randomUUID().toString(),
                             content = replyText,
                             fromId = "CurrentUser",
                             timeStamp = System.currentTimeMillis()
                         )
                     )
                    adapter.notifyDataSetChanged()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addComment(commentText:String){
       /* var commentId = UUID.randomUUID().toString()
        val commentMap = hashMapOf(
            "commentId" to commentId,
            "content" to commentText,
            "timeStamp" to System.currentTimeMillis(),
            "videoTimeStamp" to player.currentPosition,
            "fromId" to UserSession.user.id,
            "videoLink" to videoUri,
            "toId" to args.story.userId,
            "type" to "video"
        )*/
    }

    private fun setAdapter() {
        binding.apply {
            val layoutManager =
                LinearLayoutManager(this@TestActivity, LinearLayoutManager.VERTICAL, false)
            commentRv.layoutManager = layoutManager
            val reversedCommentsList = commentsList!!.reversed()
            adapter = TestCommentAdapter(
                this@TestActivity,
                commentsList
            ){
                showReplyInput(it)
            }
            commentRv.adapter = adapter
        }
    }
}