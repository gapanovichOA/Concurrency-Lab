package com.example.concurrencylab.parallelapi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

class ParallelApiViewModel : ViewModel() {

    data class ProfileState(
        val bio: String = "-",
        val posts: String = "-",
        val friends: String = "-",
        val loadingTime: Long = 0,
        val method: String = "None"
    )

    private val _uiState = MutableStateFlow(ProfileState())
    val uiState = _uiState.asStateFlow()

    /**
     * 1. 🐌 SEQUENTIAL (The Anti-Pattern)
     * Time: T1 + T2 + T3 (~4.5s)
     */
    fun loadSequentially() = viewModelScope.launch {
        val start = System.currentTimeMillis()
        reset("Sequential")
        val bio = fetchBio()
        val posts = fetchPosts()
        val friends = fetchFriends()
        updateState(bio, posts, friends.toString(), start)
    }

    /**
     * 2. ⚡ ASYNC/AWAIT (The Standard)
     * Time: Max(T1, T2, T3) (~2s)
     * Best for: One-shot results where you need all data before proceeding.
     */
    fun loadAsync() = viewModelScope.launch {
        val start = System.currentTimeMillis()
        reset("Async/Await")
        val bio = async { fetchBio() }
        val posts = async { fetchPosts() }
        val friends = async { fetchFriends() }
        updateState(bio.await(), posts.await(), friends.await().toString(), start)
    }

    /**
     * 3. 🤐 FLOW ZIP (The Pair)
     * Time: Max(T1, T2, T3)
     * Best for: Combining exactly two or more streams.
     * Note: Zip waits for a pair. if one flow emits 2 values and the other emits 1, zip stops.
     */
    fun loadZip() = viewModelScope.launch {
        val start = System.currentTimeMillis()
        reset("Flow Zip")
        val bioFlow = flow { emit(fetchBio()) }
        val postsFlow = flow { emit(fetchPosts()) }

        bioFlow.zip(postsFlow) { b, p -> b to p }
            .collect { (b, p) ->
                updateState(b, p, "N/A", start)
            }
    }

    /**
     * 4. 🧩 FLOW COMBINE (The Reactive)
     * Time: Updates as soon as ANY flow emits after all have emitted at least once.
     * Best for: Dashboards where data can change independently (e.g., real-time friends count).
     */
    fun loadCombine() = viewModelScope.launch {
        val start = System.currentTimeMillis()
        reset("Flow Combine")
        val bFlow = flow { emit(fetchBio()) }
        val pFlow = flow { emit(fetchPosts()) }
        val fFlow = flow { emit(fetchFriends()) }

        combine(bFlow, pFlow, fFlow) { b, p, f ->
            Triple(b, p, f)
        }.collect { (b, p, f) ->
            updateState(b, p, f.toString(), start)
        }
    }

    /**
     * 5. 🌊 CHANNELFLOW (The Advanced)
     * Time: Incremental updates.
     * Best for: Complex parallel work where you want to show data on screen as it arrives.
     */
    fun loadChannelFlow() = viewModelScope.launch {
        val start = System.currentTimeMillis()
        reset("ChannelFlow")

        channelFlow {
            // Launch 3 concurrent children within the channel scope
            launch { send("BIO" to fetchBio()) }
            launch { send("POSTS" to fetchPosts()) }
            launch { send("FRIENDS" to fetchFriends().toString()) }
        }.collect { (type, value) ->
            // Update the UI immediately as each individual result arrives
            _uiState.update { currentState ->
                when (type) {
                    "BIO" -> currentState.copy(bio = value)
                    "POSTS" -> currentState.copy(posts = value)
                    "FRIENDS" -> currentState.copy(friends = value)
                    else -> currentState
                }.copy(loadingTime = System.currentTimeMillis() - start)
            }
        }
    }

    // Mock API delays
    private suspend fun fetchBio() = delay(1000).run { "Senior Dev" }
    private suspend fun fetchPosts() = delay(1500).run { "12 Posts" }
    private suspend fun fetchFriends() = delay(2000).run { 1024 }

    private fun reset(method: String) {
        _uiState.value = ProfileState(method = method)
    }

    private fun updateState(b: String, p: String, f: String, start: Long) {
        _uiState.update {
            it.copy(
                bio = b,
                posts = p,
                friends = f,
                loadingTime = System.currentTimeMillis() - start
            )
        }
    }
}