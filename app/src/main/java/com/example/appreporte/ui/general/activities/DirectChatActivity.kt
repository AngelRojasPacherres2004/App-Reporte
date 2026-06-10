package com.example.appreporte.ui.general

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DirectChatActivity : AppCompatActivity() {

    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: TextInputEditText
    private lateinit var btnSend: FloatingActionButton
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<UIMessage>()
    private val firestore = FirebaseFirestore.getInstance()

    private var currentEmail: String = ""
    private var targetEmail: String = ""
    private var chatId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_direct_chat)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        currentEmail = intent.getStringExtra("CURRENT_EMAIL") ?: ""
        targetEmail = intent.getStringExtra("TARGET_EMAIL") ?: ""

        if (currentEmail.isEmpty() || targetEmail.isEmpty()) {
            Toast.makeText(this, "Error al cargar el chat", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        toolbar.title = targetEmail

        // Create a unique chat ID based on the two emails (sorted to be consistent)
        val emails = listOf(currentEmail, targetEmail).sorted()
        chatId = "${emails[0]}_${emails[1]}"

        rvMessages = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        setupRecyclerView()
        loadMessages()

        btnSend.setOnClickListener {
            val text = etMessage.text?.toString()?.trim() ?: ""
            if (text.isNotEmpty()) {
                sendMessage(text)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(messages)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        rvMessages.layoutManager = layoutManager
        rvMessages.adapter = adapter
    }

    private fun loadMessages() {
        firestore.collection("direct_chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                messages.clear()
                snapshot?.documents?.forEach { doc ->
                    val sender = doc.getString("sender") ?: ""
                    val content = doc.getString("content") ?: ""
                    messages.add(UIMessage(sender, content, sender == currentEmail))
                }
                adapter.notifyDataSetChanged()
                if (messages.isNotEmpty()) {
                    rvMessages.scrollToPosition(messages.size - 1)
                }
            }
    }

    private fun sendMessage(content: String) {
        val msgMap = hashMapOf(
            "sender" to currentEmail,
            "content" to content,
            "timestamp" to System.currentTimeMillis()
        )
        
        val chatRef = firestore.collection("direct_chats").document(chatId)
        
        // Ensure participants array is there
        chatRef.set(hashMapOf("participants" to listOf(currentEmail, targetEmail)), com.google.firebase.firestore.SetOptions.merge())
        
        chatRef.collection("messages").add(msgMap)
            .addOnSuccessListener {
                etMessage.text?.clear()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al enviar", Toast.LENGTH_SHORT).show()
            }
    }
}

