package com.turkuaz.beta.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turkuaz.beta.data.ApiClient
import com.turkuaz.beta.data.UserMemoryItem
import kotlinx.coroutines.launch

/**
 * Kullaniciya ozel Kullanici Hafizasi ekrani: goruntule / ekle / sil ("bunu
 * unut") / disa aktar (Bolum 0.3: "kullanici hafizasini gorunebilir,
 * silebilir, disa aktarabilir").
 */
@Composable
fun MemoryScreen(apiClient: ApiClient) {
    val items = remember { mutableStateListOf<UserMemoryItem>() }
    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var exportedText by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun reload() {
        try {
            val list = apiClient.getUserMemory()
            items.clear()
            items.addAll(list)
        } catch (e: Exception) {
            statusMessage = "Yükleme hatası: ${e.message}"
        }
    }

    LaunchedEffect(Unit) { reload() }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Row {
            Text("Kişisel Hafızam", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = {
                scope.launch {
                    try {
                        exportedText = apiClient.exportUserMemory()
                    } catch (e: Exception) {
                        statusMessage = "Dışa aktarma hatası: ${e.message}"
                    }
                }
            }) { Text("Dışa Aktar") }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(Modifier.weight(1f)) {
            items(items, key = { it.id }) { item ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(item.key, style = MaterialTheme.typography.labelLarge)
                            Text(item.value, style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(onClick = {
                            scope.launch {
                                try {
                                    apiClient.deleteUserMemory(item.id)
                                    reload()
                                } catch (e: Exception) {
                                    statusMessage = "Silme hatası: ${e.message}"
                                }
                            }
                        }) { Icon(Icons.Default.Delete, contentDescription = "Unut") }
                    }
                }
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text("Yeni hafıza ekle", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = newKey, onValueChange = { newKey = it },
            label = { Text("Anahtar (ör. favori_renk)") },
            singleLine = true, modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = newValue, onValueChange = { newValue = it },
            label = { Text("Değer") },
            singleLine = true, modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Button(
            enabled = newKey.isNotBlank() && newValue.isNotBlank(),
            onClick = {
                scope.launch {
                    try {
                        apiClient.writeUserMemory(newKey, newValue)
                        newKey = ""; newValue = ""
                        reload()
                    } catch (e: Exception) {
                        statusMessage = "Kaydetme hatası: ${e.message}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Kaydet") }

        statusMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }

    exportedText?.let { text ->
        AlertDialog(
            onDismissRequest = { exportedText = null },
            confirmButton = { TextButton(onClick = { exportedText = null }) { Text("Kapat") } },
            title = { Text("Dışa Aktarılan Hafıza (JSON)") },
            text = { Text(text) },
        )
    }
}
