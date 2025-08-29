package com.example.testapp.notif

object NotifBus {
    private val _events =
        kotlinx.coroutines.flow.MutableSharedFlow<PlaybackUiEvent>(extraBufferCapacity = 16)
    val events = _events
    fun emit(e: PlaybackUiEvent) {
        _events.tryEmit(e)
    }
}