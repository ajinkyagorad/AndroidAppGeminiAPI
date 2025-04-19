package com.example.geminiapi

import android.graphics.Bitmap

/**
 * Represents a message in the AI Agent chat interface
 * 
 * @property content The text content of the message
 * @property isUser Whether the message is from the user (true) or AI (false)
 * @property image Optional image attached to the message
 * @property timestamp When the message was created
 */
data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val image: Bitmap? = null,
    val timestamp: Long = System.currentTimeMillis()
)
