package com.example.concurrencylab.racecondition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.atomic.AtomicInteger


class RaceConditionViewModel(
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    companion object {
        private const val NUM_COROUTINES = 1000
        private const val INCREMENTS_PER_COROUTINE = 100
        const val TARGET_VALUE = NUM_COROUTINES * INCREMENTS_PER_COROUTINE
    }

    data class RaceUiState(
        val unsafeCount: Int = 0,
        val atomicCount: Int = 0,
        val mutexCount: Int = 0,
        val semaphoreCount: Int = 0,
        val channelCount: Int = 0,
        val actorCount: Int = 0,
        val flowCount: Int = 0,
        val isRunning: Boolean = false
    )

    private val _uiState = MutableStateFlow(RaceUiState())
    val uiState = _uiState.asStateFlow()

    private val atomicInt = AtomicInteger(0)
    private val semaphore = Semaphore(permits = 1) // Acts like a lock with 1 permit
    private val mutex = Mutex()
    private var rawUnsafeCounter = 0

    // Actor Message Definition
    sealed class CounterMsg {
        object Increment : CounterMsg()
        class GetValue(val response: CompletableDeferred<Int>) : CounterMsg()
    }

    /**
     * Executes the concurrency test.
     * We use [Dispatchers.Default] to ensure execution on a multi-threaded pool,
     * which is necessary to trigger actual race conditions that might be
     * masked on a single-threaded dispatcher.
     */
    fun runTest() {
        if (_uiState.value.isRunning) return

        resetState()

        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = true) }

            val counterActor = setupActor()

            val incrementChannel = Channel<Unit>(Channel.UNLIMITED)
            val channelJob = launch(defaultDispatcher) {
                for (msg in incrementChannel) {
                    _uiState.update { it.copy(channelCount = it.channelCount + 1) }
                }
            }

            val sharedFlow = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 100000)
            val flowJob = launch(defaultDispatcher) {
                sharedFlow.collect {
                    _uiState.update { it.copy(flowCount = it.flowCount + 1) }
                }
            }

            val jobs = List(NUM_COROUTINES) {
                launch(defaultDispatcher) {
                    repeat(INCREMENTS_PER_COROUTINE) {
                        performUnsafeUpdate()
                        performAtomicUpdate()
                        performMutexUpdate()

                        semaphore.withPermit {
                            _uiState.update { it.copy(semaphoreCount = it.semaphoreCount + 1) }
                        }

                        incrementChannel.send(Unit)

                        counterActor.send(CounterMsg.Increment)

                        sharedFlow.emit(Unit)
                    }
                }
            }

            jobs.joinAll()
            val finalActorVal = CompletableDeferred<Int>()
            counterActor.send(CounterMsg.GetValue(finalActorVal))
            _uiState.update { it.copy(actorCount = finalActorVal.await(), isRunning = false) }

            // Cleanup
            incrementChannel.close()
            channelJob.join()
            flowJob.cancel()
            counterActor.close()
        }
    }

    /**
     * ❌ UNSAFE: Demonstrates a "Lost Update."
     * 'value++' is a read-modify-write operation that is not atomic.
     * Concurrent threads will read the same stale value and overwrite each other's updates.
     */
    private fun performUnsafeUpdate() {
        rawUnsafeCounter++
        _uiState.update { it.copy(unsafeCount = rawUnsafeCounter) }
    }

    /**
     * ✅ SAFE (Lock-Free): Uses AtomicInteger's CAS (Compare-and-Swap).
     * This is a hardware-level atomic instruction. We then sync the UI state.
     */
    private fun performAtomicUpdate() {
        val newValue = atomicInt.incrementAndGet()
        _uiState.update { it.copy(atomicCount = newValue) }
    }

    /**
     * ✅ SAFE (Suspension-based): Uses a non-blocking Mutex.
     * Unlike 'synchronized', this suspends the coroutine instead of blocking the thread,
     * making it the idiomatic choice for Kotlin Coroutines.
     */
    private suspend fun performMutexUpdate() {
        mutex.withLock {
            _uiState.update { it.copy(mutexCount = it.mutexCount + 1) }
        }
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    private fun CoroutineScope.setupActor() = actor<CounterMsg>(defaultDispatcher) {
        var counter = 0
        for (msg in channel) {
            when (msg) {
                is CounterMsg.Increment -> counter++
                is CounterMsg.GetValue -> msg.response.complete(counter)
            }
        }
    }

    private fun resetState() {
        atomicInt.set(0)
        rawUnsafeCounter = 0
        _uiState.value = RaceUiState()
    }
}