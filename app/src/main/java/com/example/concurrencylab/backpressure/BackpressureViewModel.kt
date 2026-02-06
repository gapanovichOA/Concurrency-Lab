package com.example.concurrencylab.backpressure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BackpressureViewModel : ViewModel() {

    private val _events = MutableStateFlow<List<String>>(emptyList())
    val events = _events.asStateFlow()

    private val _currentValue = MutableStateFlow(0)
    val currentValue = _currentValue.asStateFlow()

    // A fast producer: 20 items, one every 100ms
    private val fastProducer = flow {
        repeat(20) {
            delay(100)
            emit(it + 1)
        }
    }

    /**
     * 1. 🛑 NO BUFFERING (Default)
     * Total time: ~10 seconds (20 * 500ms).
     * The producer is forced to wait for the slow consumer.
     */
    fun runDefault() = startTest("Default (Sequential)") {
        fastProducer.collect { process(it) }
    }

    /**
     * 2. 🗄️ BUFFER
     * Total time: ~10 seconds, but the producer finishes early.
     * The producer "shoots" all values into a buffer so it can finish its job,
     * but the consumer still processes every single one.
     */
    fun runBuffer() = startTest("Buffer") {
        fastProducer.buffer().collect { process(it) }
    }

    /**
     * 3. 🏃 COLLECT LATEST
     * The "Modern UI" approach.
     * If a new value arrives while the consumer is busy, the consumer
     * CANCELS the current work and starts fresh with the new value.
     */
    fun runCollectLatest() = startTest("CollectLatest") {
        fastProducer.collectLatest { process(it) }
    }

    /**
     * 4. 🤏 CONFLATE
     * The "Stock Ticker" approach.
     * The consumer never gets interrupted, but it skips intermediate values.
     * It only cares about the absolute latest value available when it's ready.
     */
    fun runConflate() = startTest("Conflate") {
        fastProducer.conflate().collect { process(it) }
    }

    /**
     * 5. ⏱️ DEBOUNCE (The Search Bar King)
     * Only emits a value if a specific time (300ms) passes without another emission.
     * Result: Only the very last value (20) will likely be emitted.
     */
    @OptIn(FlowPreview::class)
    fun runDebounce() = startTest("Debounce (300ms)") {
        fastProducer
            .onEach { addLog("Emitter sent: $it") }
            .debounce(300)
            .collect { process(it) }
    }

    /**
     * 📸 SAMPLE (The Heartbeat)
     * Periodically looks at the flow and emits the latest value at that moment.
     * Result: You get a "snapshot" every 500ms.
     */
    @OptIn(FlowPreview::class)
    fun runSample() = startTest("Sample (500ms)") {
        fastProducer
            .onEach { addLog("Emitter sent: $it") }
            .sample(500)
            .collect { process(it) }
    }

    private suspend fun process(value: Int) {
        _currentValue.value = value
        addLog("Processing: $value")
        delay(500) // Simulate slow UI/Database work
        addLog("✅ Finished: $value")
    }

    private fun startTest(name: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            _events.value = listOf("🚀 Starting $name...")
            val start = System.currentTimeMillis()
            block()
            addLog("🏁 Total Time: ${System.currentTimeMillis() - start}ms")
        }
    }

    private fun addLog(msg: String) {
        _events.update { it + msg }
    }
}