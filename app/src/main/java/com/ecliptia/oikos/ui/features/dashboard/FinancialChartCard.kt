package com.ecliptia.oikos.ui.features.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

// Data class to represent a slice in the pie chart
data class PieChartEntry(val value: Float, val color: Color, val label: String)

@Composable
fun FinancialChartCard(state: DashboardState) {

    val pieChartData = listOf(
        PieChartEntry(state.totalAllocated.toFloat(), Color(0xFFE91E63), "Alocado"),
        PieChartEntry(state.totalExpense.toFloat(), Color(0xFF9C27B0), "Despesas"),
        PieChartEntry(state.currentBalance.toFloat(), Color(0xFF2196F3), "Saldo")
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Distribuição da Renda", style = MaterialTheme.typography.titleMedium)
            SimplePieChart(
                entries = pieChartData,
                modifier = Modifier.fillMaxWidth().height(200.dp),
                chartSize = 200f // Adjust as needed
            )
        }
    }
}

@Composable
fun SimplePieChart(
    entries: List<PieChartEntry>,
    modifier: Modifier = Modifier,
    chartSize: Float = 200f, // Diameter of the pie chart
    centerText: String? = null
) {
    val totalValue = entries.sumOf { it.value.toDouble() }.toFloat()
    var startAngle = 0f

    Box(
        modifier = modifier.size(chartSize.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val radius = chartSize.dp.toPx() / 2f
            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

            entries.forEach { entry ->
                val sweepAngle = (entry.value / totalValue) * 360f

                drawArc(
                    color = entry.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )

                // Optional: Draw labels outside the pie chart
                val angleInRadians = (startAngle + sweepAngle / 2).toDouble() * Math.PI / 180.0
                val labelRadius = radius * 0.7f // Position labels slightly inside the radius
                val x = center.x + labelRadius * cos(angleInRadians).toFloat()
                val y = center.y + labelRadius * sin(angleInRadians).toFloat()

                drawIntoCanvas {
                    it.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 12.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        drawText(
                            "${(entry.value / totalValue * 100).toInt()}%",
                            x,
                            y + paint.textSize / 3, // Adjust y for vertical centering
                            paint
                        )
                    }
                }

                startAngle += sweepAngle
            }
        }

        // Optional: Center text
        centerText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}