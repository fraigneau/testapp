package com.example.testapp.ui.component.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testapp.data.DataStoreProvider
import com.example.testapp.ui.component.Greeting
import com.example.testapp.ui.component.TrackList
import com.example.testapp.viewmodel.TrackViewModel

@Composable
fun DataSection(
    dataStoreProvider: DataStoreProvider,
    trackViewModel: TrackViewModel,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(50.dp) // Padding général

        ) {
            Text(
                text = "Test DataStore",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.error
            )
            Greeting(
                name = dataStoreProvider.getUserFlow,
                age = dataStoreProvider.getUserAgeFlow,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "Test Room",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.error
            )
            TrackList(
                tracks = trackViewModel.tracks.collectAsState(initial = emptyList()).value
            )
        }
    }
}

