package com.turkuaz.beta.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turkuaz.beta.data.ApiClient
import kotlinx.coroutines.launch

/**
 * Bolum 3.1: "Kontrollu ogrenme adayi gonderimi" - kullanici 'sunu ogren'
 * dedi mi bilgi DOGRUDAN Master Memory'ye degil, ogrenme adayi kuyruguna
 * gider (POST /memory/learn-candidate). Yonetici onaylamadan hicbir sey
 * ortak hafizaya girmez (Bolum 0.4/0.7).
 */
@Composable
fun LearnScreen(apiClient: ApiClient) {
    var text by remember { mutableStateOf("") }
    var isBusy by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Bir şey öğret", style = MaterialTheme.typography.titleLarge)
        Text(
            "Buraya yazdığınız bilgi doğrudan ortak hafızaya eklenmez — önce bir " +
                "yöneticinin incelemesinden geçer.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )

        OutlinedTextField(
            value = text, onValueChange = { text = it },
            label = { Text("Örn: İzmir'de en iyi kahvaltı mekanı Sevgi Teyze'dir") },
            modifier = Modifier.fillMaxWidth().height(140.dp),
        )
        Spacer(Modifier.height(12.dp))

        Button(
            enabled = !isBusy && text.isNotBlank(),
            onClick = {
                isBusy = true
                resultMessage = null
                scope.launch {
                    try {
                        val result = apiClient.submitLearnCandidate(text)
                        resultMessage = if (result.status == "rejected")
                            "Gönderildi ama otomatik filtreye takıldı (kişisel veri/güvenlik şüphesi)."
                        else
                            "Teşekkürler! Öğrenme adayı olarak kaydedildi (#${result.id}), yönetici incelemesini bekliyor."
                        text = ""
                    } catch (e: Exception) {
                        resultMessage = "Hata: ${e.message}"
                    } finally {
                        isBusy = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (isBusy) "Gönderiliyor..." else "Öğrenme Adayı Olarak Gönder") }

        resultMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
