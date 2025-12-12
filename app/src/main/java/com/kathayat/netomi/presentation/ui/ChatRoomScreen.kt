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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kathayat.netomi.domain.model.ChatMessage
import com.kathayat.netomi.presentation.vm.ChatRoomViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatRoomScreen(
    innerPaddingValues: PaddingValues,
    chatId: Int,
    viewModel: ChatRoomViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val messages by viewModel.messages.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(chatId) {
        viewModel.loadMessages(chatId)
        viewModel.markAsRead(chatId)
    }

    LaunchedEffect(Unit) {
        viewModel.error.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        modifier = Modifier.padding(innerPaddingValues),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopBar(
                onBack = onBack,
                isOnline = viewModel.isOnline.collectAsState().value
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
                .imePadding()
        ) {

            LazyColumn(
                modifier = Modifier.weight(1f),
                reverseLayout = false
            ) {
                items(messages) { msg ->
                    MessageBubble(msg)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
fun ChatInput(
    onSend: (String) -> Unit,
    onRetry: () -> Unit
) {
    var text by remember { mutableStateOf(TextFieldValue("")) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Type a message…") },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = {
                if (text.text.isNotBlank()) {
                    onSend(text.text)
                    text = TextFieldValue("")
                }
            }
        ) {
            Text("Send")
        }

        Spacer(modifier = Modifier.width(8.dp))

        TextButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun TopBar(
    onBack: () -> Unit,
    isOnline: Boolean
) {
    Column {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "Back",
                modifier = Modifier.clickable { onBack() },
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Chat Room",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Offline banner
        if (!isOnline) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFC2C2))
                    .padding(10.dp)
            ) {
                Text(
                    "Offline mode — messages queued",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


@Composable
fun MessageBubble(message: ChatMessage) {
    val time = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault())
            .format(Date(message.timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp)
    ) {

        Text(
            text = "${message.sender} • $time",
            style = MaterialTheme.typography.labelSmall
        )

        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 2.dp,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = if(message.message.contains("error")) "Connected to WS" else message.message,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
