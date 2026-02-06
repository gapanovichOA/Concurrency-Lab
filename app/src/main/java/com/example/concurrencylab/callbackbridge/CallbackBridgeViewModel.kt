package com.example.concurrencylab.callbackbridge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CallbackBridgeViewModel : ViewModel() {

    val sensorData: StateFlow<String> = getMockSensorFlow()
        .map { "Sensor Value: $it" }
        .stateIn(
            scope = viewModelScope,
            // It stops the sensor 5s after the user leaves the screen.
            // If they come back within 5s (like rotation), it keeps running.
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "Sensor Connecting..."
        )

    /**
     * ✅ THE BRIDGE: callbackFlow
     * Converts a multi-shot callback API into a cold Flow.
     */
    private fun getMockSensorFlow() = callbackFlow<Int> {
        val listener = object : MockSensorListener {
            override fun onSensorChanged(value: Int) {
                // 'trySend' is used to push data from non-coroutine worlds
                // It's safer than 'send' inside a callback to avoid blocking
                trySend(value)
            }
        }

        MockSensorApi.register(listener)
        println("SENSOR: Listener Registered")

        /**
         * awaitClose
         * This is CRITICAL. It keeps the flow alive while the collector is active.
         * When the collector (UI) is cancelled, this block runs.
         */
        awaitClose {
            MockSensorApi.unregister(listener)
            println("SENSOR: Listener Unregistered (No Leak!)")
        }
    }
}

// Mock API to simulate external callback-based system
interface MockSensorListener {
    fun onSensorChanged(value: Int)
}

object MockSensorApi {
    private var job: Job? = null
    fun register(listener: MockSensorListener) {
        job = CoroutineScope(Dispatchers.Default).launch {
            var i = 0
            while (true) {
                delay(1000)
                listener.onSensorChanged(i++)
            }
        }
    }

    fun unregister(listener: MockSensorListener) {
        job?.cancel()
    }
}