package com.example.testapp.ui.component

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.testapp.entity.TrackEntity

@Composable
fun TrackList(tracks: List<TrackEntity>) {
    LazyColumn {
        items(tracks) { track ->
            Text(text = "${track.title} - ${track.artist}")
        }
    }
}