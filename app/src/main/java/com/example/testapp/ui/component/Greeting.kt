package com.example.testapp.ui.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.Flow

@Composable
fun Greeting(name: Flow<String>, modifier: Modifier = Modifier, age: Flow<String>) {
    val userName = name.collectAsState(initial = "Chargement...")
    val userAge = age.collectAsState(initial = "Âge inconnu")

    Text(
        text = "Hello ${userName.value}! age is ${userAge.value}",
        modifier = modifier
    )
}