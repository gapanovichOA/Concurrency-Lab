package com.example.concurrencylab.parallelapi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ParallelApiScreen(viewModel: ParallelApiViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .systemBarsPadding()
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Parallel Strategies", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Active Method: ${state.method}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (state.loadingTime > 3000)
                    Color.Red.copy(alpha = 0.1f) else Color.Green.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Total Loading Time: ${state.loadingTime}ms",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DataRow(label = "User Bio", value = state.bio)
            DataRow(label = "Recent Posts", value = state.posts)
            DataRow(label = "Friends Count", value = state.friends)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Select Strategy to Run:", style = MaterialTheme.typography.labelLarge)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StrategyButton("1. Sequential (Slowest)", Color.Gray) { viewModel.loadSequentially() }
            StrategyButton("2. Async/Await (Fast)", Color(0xFF6200EE)) { viewModel.loadAsync() }
            StrategyButton("3. Flow Zip (Paired)", Color(0xFF03DAC5)) { viewModel.loadZip() }
            StrategyButton(
                "4. Flow Combine (Reactive)",
                Color(0xFF3700B3)
            ) { viewModel.loadCombine() }
            StrategyButton(
                "5. ChannelFlow (Incremental)",
                Color(0xFF018786)
            ) { viewModel.loadChannelFlow() }
        }
    }
}

@Composable
private fun DataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.Medium)
        Text(
            text = value,
            color = if (value == "-") Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StrategyButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(label, color = Color.White)
    }
}