package com.example.geminiapi

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AIAgentViewModel : ViewModel() {
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey
    )
    
    // Initialize with a welcome message
    init {
        _messages.value = listOf(
            ChatMessage(
                content = "Hello! I'm your AI assistant. I can help with questions, see images, and have conversations. What would you like to do?",
                isUser = false
            )
        )
    }
    
    fun sendTextMessage(text: String) {
        // Add user message to the list
        val userMessage = ChatMessage(
            content = text,
            isUser = true
        )
        _messages.value = _messages.value + userMessage
        
        // Generate AI response
        _isLoading.value = true
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(
                    content {
                        text(text)
                    }
                )
                
                val aiResponse = response.text ?: "I'm not sure how to respond to that."
                
                withContext(Dispatchers.Main) {
                    val aiMessage = ChatMessage(
                        content = aiResponse,
                        isUser = false
                    )
                    _messages.value = _messages.value + aiMessage
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMessage = ChatMessage(
                        content = "Sorry, I encountered an error: ${e.localizedMessage ?: "Unknown error"}",
                        isUser = false
                    )
                    _messages.value = _messages.value + errorMessage
                    _isLoading.value = false
                }
            }
        }
    }
    
    fun sendImageMessage(image: Bitmap, prompt: String = "What do you see in this image?") {
        // Add user message with image
        val userMessage = ChatMessage(
            content = prompt,
            isUser = true,
            image = image
        )
        _messages.value = _messages.value + userMessage
        
        // Generate AI response based on image
        _isLoading.value = true
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(
                    content {
                        image(image)
                        text(prompt)
                    }
                )
                
                val aiResponse = response.text ?: "I'm not sure what I'm seeing in this image."
                
                withContext(Dispatchers.Main) {
                    val aiMessage = ChatMessage(
                        content = aiResponse,
                        isUser = false
                    )
                    _messages.value = _messages.value + aiMessage
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMessage = ChatMessage(
                        content = "Sorry, I couldn't analyze the image: ${e.localizedMessage ?: "Unknown error"}",
                        isUser = false
                    )
                    _messages.value = _messages.value + errorMessage
                    _isLoading.value = false
                }
            }
        }
    }
}
