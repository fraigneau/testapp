package com.example.testapp.viewmodel

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.DatabaseProvider
import com.example.testapp.entity.TrackEntity
import com.example.testapp.repository.TrackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TrackViewModel (
    private val databaseProvider: DatabaseProvider,
) : ViewModel() {

    private val _tracks = MutableStateFlow<List<TrackEntity>>(emptyList())
    private val trackRepository =
        TrackRepository(databaseProvider.getAppDatabase().trackDao())  // Utilise le TrackDao

    val tracks: StateFlow<List<TrackEntity>> = _tracks
    val track: List<TrackEntity>
        get() = _tracks.value

    fun loadTracks() {
        viewModelScope.launch (Dispatchers.IO) {
            val db = databaseProvider.getAppDatabase()
            val trackDao = db.trackDao()
            delay(1000)
            _tracks.value = trackDao.getAllTracks()

        }
    }

    @Deprecated("Use loadTracks() instead", ReplaceWith("loadTracks()"))
    fun pushTracksToDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val db = databaseProvider.getAppDatabase()
            trackRepository.pushTracksToDatabase(db)
        }
    }
}
