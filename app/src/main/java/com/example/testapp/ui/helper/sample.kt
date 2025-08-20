package com.example.testapp.ui.helper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.Pie


data class PieChartData(
    val label: String,
    val value: Double,
    val color: Color,
    val selectedColor: Color = color,
    val selected: Boolean = false,
)
fun PieChartData.toPie(): Pie = Pie(label = label, data = value, color = color, selectedColor = selectedColor, selected = selected)

data class BarData(
    val x: String,           // catégorie (mois, etc.)
    val y: Double,           // valeur
    val series: String? = null, // nom de série optionnel
    val color: Color,
)
fun List<BarData>.toBars(): List<Bars> =
    groupBy { it.x }.map { (x, items) ->
        Bars(
            label = x,
            values = items.map { Bars.Data(label = it.series ?: "", value = it.y, color = SolidColor(it.color)) }
        )
    }


// --- Samples (si besoin) ---
fun samplePieData() = listOf(
    PieChartData("Bleu",   40.0, Color(0xFF2196F3)),
    PieChartData("Orange", 30.0, Color(0xFFFF9800)),
    PieChartData("Vert",   20.0, Color(0xFF4CAF50)),
    PieChartData("Rouge",  10.0, Color(0xFFF44336)),
)

fun sampleBarData() = listOf(
    BarData("Jan", 24.0, "A", Color(0xFF90CAF9)),
    BarData("Jan", 18.0, "B", Color(0xFF1E88E5)),
    BarData("Fév", 31.0, "A", Color(0xFF81C784)),
    BarData("Fév", 27.0, "B", Color(0xFF43A047)),
    BarData("Mar", 28.0, "A", Color(0xFF64B5F6)),
    BarData("Mar", 22.0, "B", Color(0xFF1976D2)),
)