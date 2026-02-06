package com.example.concurrencylab.callbackbridge

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CallbackBridgeScreen(viewModel: CallbackBridgeViewModel) {
    val sensorData by viewModel.sensorData.collectAsStateWithLifecycle()

    Column(modifier = Modifier
        .systemBarsPadding()
        .padding(16.dp)) {
        Text("Callback-to-Flow Bridge", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(text = sensorData, style = MaterialTheme.typography.titleLarge)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "This lab wraps a legacy listener in a callbackFlow. " +
                    "If you navigate back, 'awaitClose' will automatically unregister the listener.after 5 seconds.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}