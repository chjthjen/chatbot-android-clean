package com.example.chatbot.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbot.data.model.Chat
import com.example.chatbot.data.model.ChatHistoryItem
import com.example.chatbot.domain.repository.AuthRepository
import com.example.chatbot.domain.repository.ChatRepository
import com.example.chatbot.presentation.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,

    ) : ViewModel() {

    private val _chatHistory = MutableLiveData<Resource<List<ChatHistoryItem>>>(Resource.Unspecified())
    val chatHistory: LiveData<Resource<List<ChatHistoryItem>>> = _chatHistory
    private val userUid = authRepository.getCurrentUser()?.uid
    companion object {
        private const val TAG = "HomeViewModel"
    }

    init {
        fetchChats()
    }


    private fun fetchChats() {
        viewModelScope.launch {
            _chatHistory.value = Resource.Loading()
            chatRepository.fetchAllChats().collect{chats ->
                val groupedChats = chats.groupBy { chat ->
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    dateFormat.format(chat.timestamp ?: Date())
                }.map { (dateString, chatList) ->
                    ChatHistoryItem(dateString, chatList, chats.firstOrNull{it.timestamp != null}?.timestamp)
                }.sortedByDescending {
                    it.date
                }
                _chatHistory.value = Resource.Success(groupedChats)
                Log.d(TAG, "fetchChats: chats=$groupedChats")
            }
        }
    }
    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            chatRepository.deleteChat(chatId)
        }
    }
}