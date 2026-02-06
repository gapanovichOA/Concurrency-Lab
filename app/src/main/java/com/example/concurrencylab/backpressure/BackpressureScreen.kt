package com.example.concurrencylab.backpressure

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun BackpressureScreen(viewModel: BackpressureViewModel) {
    val logs by viewModel.events.collectAsStateWithLifecycle()
    val current by viewModel.currentValue.collectAsStateWithLifecycle()

    Column(modifier = Modifier
        .systemBarsPadding()
        .padding(16.dp)) {
        Text("Backpressure & Buffering", style = MaterialTheme.typography.headlineMedium)

        // Visual Indicator of current value
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                current.toString(),
                color = Color.Green,
                style = MaterialTheme.typography.displayLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.runDefault() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) { Text("Default") }

                Button(
                    onClick = { viewModel.runBuffer() },
                    modifier = Modifier.weight(1f)
                ) { Text("Buffer") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.runConflate() },
                    modifier = Modifier.weight(1f)
                ) { Text("Conflate") }
                Button(
                    onClick = { viewModel.runCollectLatest() },
                    modifier = Modifier.weight(1f)
                ) { Text("Latest") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.runDebounce() },
                    modifier = Modifier.weight(1f)
                ) { Text("Debounce") }
                Button(
                    onClick = { viewModel.runSample() },
                    modifier = Modifier.weight(1f)
                ) { Text("Sample") }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)) {
            items(items = logs.reversed()) { item -> Text(item, fontFamily = FontFamily.Monospace) }
        }
    }
}