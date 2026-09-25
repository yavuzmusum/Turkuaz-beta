package com.turkuaz.beta.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turkuaz.beta.data.ApiClient
import kotlinx.coroutines.launch

/** Bolum 3.1: "Uygulama ici geri bildirim ve hata raporlama formu". */
@Composable
fun FeedbackScreen(apiClient: ApiClient) {
    var message by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("feedback") }
    var isBusy by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Geri Bildirim / Hata Bildir", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        Row {
            listOf("feedback" to "Öneri", "bug" to "Hata").forEach { (value, label) ->
                Row(
                    Modifier.selectable(selected = category == value, onClick = { category = value })
                        .padding(end = 16.dp),
                ) {
                    RadioButton(selected = category == value, onClick = { category = value })
                    Text(label, Modifier.padding(start = 4.dp, top = 12.dp))
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = message, onValueChange = { message = it },
            label = { Text("Mesajınız") },
            modifier = Modifier.fillMaxWidth().height(160.dp),
        )
        Spacer(Modifier.height(12.dp))

        Button(
            enabled = !isBusy && message.isNotBlank(),
            onClick = {
                isBusy = true
                scope.launch {
                    try {
                        apiClient.submitFeedback(message, category)
                        statusMessage = "Gönderildi, teşekkürler!"
                        message = ""
                    } catch (e: Exception) {
                        statusMessage = "Hata: ${e.message}"
                    } finally {
                        isBusy = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (isBusy) "Gönderiliyor..." else "Gönder") }

        statusMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
