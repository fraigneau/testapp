package com.example.testapp.ui.component.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.example.testapp.ui.helper.BarData
import com.example.testapp.ui.helper.ChartCard
import com.example.testapp.ui.helper.PieChartData
import com.example.testapp.ui.helper.PieLegend
import com.example.testapp.ui.helper.sampleBarData
import com.example.testapp.ui.helper.samplePieData
import com.example.testapp.ui.helper.toBars
import com.example.testapp.ui.helper.toPie
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Pie
import ir.ehsannarmani.compose_charts.models.PopupProperties
import ir.ehsannarmani.compose_charts.models.StrokeStyle

@Composable
fun ChartsSection(
    pieData: List<PieChartData> = samplePieData(),
    barData: List<BarData> = sampleBarData(),
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ChartCard(
            title = "Répartition",
            subtitle = "Par catégorie"
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .padding(12.dp)
            ) {
                val pies = remember(pieData) { pieData.map { it.toPie() } }
                PieChart(
                    modifier = Modifier.fillMaxSize(),
                    data = pies,
                    style = Pie.Style.Stroke(width = 36.dp),
                    spaceDegree = 3f,
                    selectedPaddingDegree = 2f,
                    selectedScale = 1.05f,
                    onPieClick = { /* optionnel: gérer la sélection si tu veux */ }
                )
            }
            Spacer(Modifier.height(12.dp))
            PieLegend(pieData)
        }

        ChartCard(
            title = "Volumes",
            subtitle = "3 derniers mois"
        ) {
            ColumnChart(
                data = remember(barData) { barData.toBars() },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(top = 8.dp),

                barProperties = BarProperties(
                    thickness = 22.dp,
                    spacing = 6.dp,
                    cornerRadius = Bars.Data.Radius.Circular(8.dp)
                ),

                labelProperties = LabelProperties(
                    enabled = true,
                    textStyle = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    rotation = LabelProperties.Rotation(
                        mode = LabelProperties.Rotation.Mode.Force,
                        degree = -35f
                    ),

                ),

                indicatorProperties = HorizontalIndicatorProperties(
                    enabled = true,
                    textStyle = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),

                ),

                gridProperties = GridProperties(
                    enabled = true,
                    xAxisProperties = GridProperties.AxisProperties(
                        enabled = true,
                        style = StrokeStyle.Normal,
                        color = SolidColor(Color.White.copy(alpha = 0.4f)),
                        thickness = 0.5.dp,
                        lineCount = 5
                    ),
                    yAxisProperties = GridProperties.AxisProperties(
                        enabled = true,
                        style = StrokeStyle.Normal,
                        color = SolidColor(Color.White.copy(alpha = 0.4f)),
                        thickness = 0.5.dp,
                        lineCount = 5
                    )
                ),

                popupProperties = PopupProperties(
                    enabled = true,
                    duration = 1500L,
                    textStyle = MaterialTheme.typography.labelSmall,
                    containerColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),

                labelHelperProperties = LabelHelperProperties(
                    enabled = true,
                    textStyle = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                )
            )
        }
    }
}