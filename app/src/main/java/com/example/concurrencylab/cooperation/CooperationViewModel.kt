package com.example.concurrencylab.cooperation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

class CooperationViewModel() : ViewModel() {

    private val _workerLog = MutableStateFlow<List<String>>(emptyList())
    val workerLog = _workerLog.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    val singleThread = Dispatchers.Default.limitedParallelism(1)


    fun runCooperationTest(isCooperative: Boolean) {
        _workerLog.value = emptyList()
        _isRunning.value = true

        viewModelScope.launch(singleThread) {
            // We launch two workers on the same dispatcher pool
            val job1 = launch { runWorker(id = 1, useYield = isCooperative) }
            val job2 = launch { runWorker(id = 2, useYield = isCooperative) }

            joinAll(job1, job2)
            _isRunning.value = false
            addLog("🏁 Both Workers Finished")
        }
    }

    private suspend fun runWorker(id: Int, useYield: Boolean) {
        repeat(5) { iteration ->
            // Simulate heavy CPU work
            val start = System.currentTimeMillis()
            while (System.currentTimeMillis() - start < 100) {
                // Busy-waiting to simulate CPU load
            }

            addLog("Worker $id: Completed step $iteration")

            if (useYield) {
                /**
                 * ✅ yield() makes the coroutine cooperative.
                 * It suspends, allowing other coroutines on the same thread
                 * a chance to execute.
                 */
                yield()
            }
        }
    }

    private fun addLog(message: String) {
        _workerLog.update { it + message }
    }
}