package com.example.concurrencylab.racecondition

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import java.text.NumberFormat

@Composable
fun RaceConditionScreen(viewModel: RaceConditionViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .systemBarsPadding()
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Target: ${NumberFormat.getIntegerInstance().format(RaceConditionViewModel.TARGET_VALUE)}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Simulating 1,000 parallel coroutines",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Card 1: Unsafe
        CounterCard(
            label = "Unsafe Counter (Standard Int)",
            currentValue = uiState.unsafeCount,
            targetValue = RaceConditionViewModel.TARGET_VALUE,
            color = Color.Red
        )

        // Card 2: Atomic
        CounterCard(
            label = "Atomic Integer (CAS)",
            currentValue = uiState.atomicCount,
            targetValue = RaceConditionViewModel.TARGET_VALUE,
            color = Color.Cyan
        )

        // Card 3: Mutex
        CounterCard(
            label = "Mutex Protected (Non-blocking)",
            currentValue = uiState.mutexCount,
            targetValue = RaceConditionViewModel.TARGET_VALUE,
            color = Color.Green
        )

        // Card 4: Semaphore
        CounterCard(
            label = "Semaphore (1 Permit)",
            currentValue = uiState.semaphoreCount,
            targetValue = RaceConditionViewModel.TARGET_VALUE,
            color = Color(0xFFFF9800)
        )

        // Card 5: Channels
        CounterCard(
            label = "Channels (Pipeline)",
            currentValue = uiState.channelCount,
            targetValue = RaceConditionViewModel.TARGET_VALUE,
            color = Color(0xFF9C27B0)
        )

        // Card 6: Actor Model
        CounterCard(
            label = "Actor (Message Passing)",
            currentValue = uiState.actorCount,
            targetValue = RaceConditionViewModel.TARGET_VALUE,
            color = Color(0xFF3F51B5)
        )

        // Card 7: SharedFlow
        CounterCard(
            label = "SharedFlow (Reactive)",
            currentValue = uiState.flowCount,
            targetValue = RaceConditionViewModel.TARGET_VALUE,
            color = Color(0xFFE91E63)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { viewModel.runTest() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isRunning
        ) {
            if (uiState.isRunning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Running Simulation...")
            } else {
                Text("Start Race Condition")
            }
        }
    }
}

@Composable
private fun CounterCard(
    label: String,
    currentValue: Int,
    targetValue: Int,
    color: Color
) {
    val isError = currentValue != targetValue && currentValue > 0
    val cardColor = if (isError) color.copy(alpha = 0.1f) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        border = BorderStroke(1.dp, if (isError) color else Color.Gray.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                Text(
                    text = NumberFormat.getIntegerInstance().format(currentValue),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (isError) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Race Condition Detected",
                    tint = color
                )
            } else if (currentValue == targetValue && currentValue > 0) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Thread Safe",
                    tint = color
                )
            }
        }
    }
}