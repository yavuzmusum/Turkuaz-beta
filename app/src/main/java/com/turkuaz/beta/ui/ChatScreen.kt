package com.turkuaz.beta.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turkuaz.beta.data.ApiClient
import kotlinx.coroutines.launch

private data class ChatLine(val fromUser: Boolean, val text: String)

@Composable
fun ChatScreen(apiClient: ApiClient) {
    var input by remember { mutableStateOf("") }
    var isBusy by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf<ChatLine>() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
            items(messages) { line ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (line.fromUser)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                ) {
                    Text(line.text, Modifier.padding(10.dp))
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            OutlinedTextField(
                value = input, onValueChange = { input = it },
                placeholder = { Text("Bir şey sor...") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            Spacer(Modifier.width(8.dp))
            Button(
                enabled = !isBusy && input.isNotBlank(),
                onClick = {
                    val msg = input
                    input = ""
                    messages.add(ChatLine(fromUser = true, text = msg))
                    isBusy = true
                    scope.launch {
                        try {
                            val reply = apiClient.sendChat(msg)
                            messages.add(ChatLine(fromUser = false, text = reply.reply))
                        } catch (e: Exception) {
                            messages.add(ChatLine(fromUser = false, text = "Hata: ${e.message}"))
                        } finally {
                            isBusy = false
                            if (messages.isNotEmpty()) {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        }
                    }
                },
            ) { Text("Gönder") }
        }
    }
}
