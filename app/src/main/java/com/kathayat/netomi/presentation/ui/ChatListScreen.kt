package com.kathayat.netomi.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kathayat.netomi.data.local.ChatEntity
import com.kathayat.netomi.presentation.vm.ChatListViewModel
import kotlin.text.ifEmpty

@Composable
fun ChatListScreen(
    innerPadding: PaddingValues,
    viewModel: ChatListViewModel = hiltViewModel(),
    onChatOpen: (Int) -> Unit
) {
    val chats by viewModel.chats.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.navigateToChat.collect { chatId ->
            onChatOpen(chatId)
        }
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    viewModel.loadChats()
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.error.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        modifier = Modifier.padding(innerPadding),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.createNewChat() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Chat")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
        ) {

            Text(
                text = "Chats",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (chats.isEmpty()) {
                EmptyChatState(viewModel)
            } else {
                ChatList(chats = chats, onChatOpen = onChatOpen)
            }
        }
    }
}

@Composable
fun EmptyChatState(viewModel: ChatListViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No Chats Available")
            Spacer(Modifier.height(8.dp))

            Button(onClick = {
                viewModel.createNewChat()   // ⬅ NEW
            }) {
                Text("Start Chat")
            }
        }
    }
}

@Composable
fun ChatList(
    chats: List<ChatEntity>,
    onChatOpen: (Int) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(chats) { chat ->
            ChatListItem(chat = chat) {
                onChatOpen(chat.chatId)
            }
        }
    }
}

@Composable
fun ChatListItem(
    chat: ChatEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {

            Text(
                text = chat.chatName,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = chat.lastMessage,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }

        if (chat.unreadCount > 0) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
        }
    }
}

fun lastMessage(lastMessage:String):String {
    var message = ""
    if (lastMessage.isEmpty()){
        message = "No messages yet"
    } else if (lastMessage.contains("error")){
        message = "Message from WS Connected"
    }
    return message
}
