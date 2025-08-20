package com.example.testapp.ui.helper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PieLegend(data: List<PieChartData>) {
    val total = data.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        data.forEach { slice ->
            val pct = (slice.value / total * 100f)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(slice.label, style = MaterialTheme.typography.bodyMedium, color = slice.color)
                Text(String.format("%.0f%%", pct), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}