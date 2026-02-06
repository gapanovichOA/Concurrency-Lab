package com.example.concurrencylab.blockingtrap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BlockingTrapViewModel : ViewModel() {

    private val _status = MutableStateFlow("System Ready")
    val status = _status.asStateFlow()

    /**
     * ❌ THE TRAP: Thread.sleep()
     * This blocks the actual underlying thread. Since it's called on
     * Dispatchers.Main, the UI thread stops dead. No animations, no clicks.
     */
    fun runBlockingTask() {
        _status.value = "🚫 BLOCKING: UI is now frozen for 3 seconds..."

        // Even though this is in a coroutine, Thread.sleep kills the thread it runs on.
        viewModelScope.launch(Dispatchers.Main) {
            Thread.sleep(3000)
            _status.value = "✅ Finished Blocking"
        }
    }

    /**
     * ✅ THE GOOD WAY: delay()
     * This 'suspends' the coroutine. It tells the thread:
     * "I'm waiting, go do other work (like drawing the UI) and come back to me later."
     */
    fun runSuspendingTask() {
        _status.value = "🚀 SUSPENDING: UI remains smooth..."

        viewModelScope.launch(Dispatchers.Main) {
            delay(3000)
            _status.value = "✅ Finished Suspending"
        }
    }
}