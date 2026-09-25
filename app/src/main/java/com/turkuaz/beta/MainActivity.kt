package com.turkuaz.beta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turkuaz.beta.data.ApiClient
import com.turkuaz.beta.data.DeviceId
import com.turkuaz.beta.data.SessionStore
import com.turkuaz.beta.ui.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = SessionStore(applicationContext)
        val fingerprint = DeviceId.get(applicationContext)
        val apiClient = ApiClient(session, fingerprint)

        setContent {
            TurkuazTheme {
                AppRoot(apiClient = apiClient, session = session)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppRoot(apiClient: ApiClient, session: SessionStore) {
    var isLoggedIn by remember { mutableStateOf(session.isLoggedIn) }
    var currentScreen by remember { mutableStateOf(Screen.Chat) }
    var experimentalBannerEnabled by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadFlags() {
        scope.launch {
            try {
                val flags = apiClient.getFeatureFlags()
                // Bolum 3.1: yoneticinin uzaktan actigi bir ornek bayrak.
                experimentalBannerEnabled = flags.any { it.key == "beta_experimental_banner" && it.enabled }
            } catch (_: Exception) {
                // Bayraklar yuklenemezse sessizce varsayilanlarla devam et.
            }
        }
    }

    if (isLoggedIn) LaunchedEffect(Unit) { loadFlags() }

    if (!isLoggedIn) {
        LoginScreen(apiClient = apiClient) { _ ->
            isLoggedIn = true
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text("TURKUAZ AI")
                        Spacer(Modifier.width(8.dp))
                        AssistChip(onClick = {}, label = { Text("BETA") })
                    }
                },
                actions = {
                    TextButton(onClick = {
                        session.clear()
                        isLoggedIn = false
                        currentScreen = Screen.Chat
                    }) { Text("Çıkış") }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                Screen.values().forEach { screen ->
                    NavigationBarItem(
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
                        icon = {},
                        label = { Text(screen.label) },
                    )
                }
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (experimentalBannerEnabled) {
                Surface(color = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Deneysel özellik aktif (yönetici tarafından uzaktan açıldı)",
                        Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            when (currentScreen) {
                Screen.Chat -> ChatScreen(apiClient)
                Screen.Memory -> MemoryScreen(apiClient)
                Screen.Learn -> LearnScreen(apiClient)
                Screen.Feedback -> FeedbackScreen(apiClient)
            }
        }
    }
}
