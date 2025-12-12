package com.kathayat.netomi.presentation.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kathayat.netomi.domain.model.ChatMessage
import com.kathayat.netomi.presentation.vm.ChatRoomViewModel
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun ChatRoomScreen(
    innerPaddingValues: PaddingValues,
    chatId: Int,
    viewModel: ChatRoomViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(chatId) {
        viewModel.loadMessages(chatId)
        viewModel.markAsRead(chatId)
    }

    LaunchedEffect(Unit) {
        viewModel.error.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        modifier = Modifier.padding(innerPaddingValues),
        topBar = {
            TopBar(onBack = onBack, isOnline = viewModel.isOnline.collectAsState().value)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
                .imePadding()
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(messages) { m ->
                    if (m.isPending) {
                        PendingMessageBubble(m, onRetry = { viewModel.retrySingle(chatId,m.id) })
                    } else {
                        MessageBubble(m)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            ChatInput(
                onSend = { text ->
                    viewModel.sendMessage(chatId, text)
                },
                onRetry = {
                    viewModel.retryPending(chatId)
                }
            )
        }
    }
}

@Composable
fun TopBar(onBack: () -> Unit, isOnline: Boolean) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Back", modifier = Modifier.clickable { onBack() })
            Text("Chat Room")
        }
        if (!isOnline) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer).padding(8.dp)
            ) {
                Text("Offline — messages will be queued")
            }
        }
    }
}

@Composable
fun MessageBubble(m: ChatMessage) {
    Column(modifier = Modifier.fillMaxWidth().padding(6.dp)) {
        Text(
            "${m.sender} • ${
                SimpleDateFormat("HH:mm").format(Date(m.timestamp))
            }", style = MaterialTheme.typography.labelSmall
        )
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 2.dp,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(m.message, modifier = Modifier.padding(12.dp))
        }
    }
}

@Composable
fun PendingMessageBubble(m: ChatMessage, onRetry: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "You • pending",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 0.dp,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        m.message,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                    RetryButton (retry = onRetry)
                }
            }
        }
    }
}

@Composable
fun ChatInput(onSend: (String) -> Unit, onRetry: () -> Unit) {
    var text by remember { mutableStateOf("") }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            placeholder = { Text("Type a message") })
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = {
            if (text.isNotBlank()) {
                onSend(text.trim())
                text = ""
            }
        }) { Text("Send") }
    }
}

@Composable
fun RetryButton(retry: () -> Unit) {
    TextButton(onClick = retry) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh Icon"
            )
            Spacer(modifier = Modifier.width(4.dp))

            Text("Retry",  color = MaterialTheme.colorScheme.error)
        }
    }
}