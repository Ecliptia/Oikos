package com.ecliptia.oikos.ui.features.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntSize
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecliptia.oikos.ui.features.dashboard.PieChartEntry
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ReportsScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Relatórios Financeiros", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Category-wise spending Pie Chart
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Gastos por Categoria", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (uiState.expensesByCategory.isNotEmpty()) {
                    val pieChartData = uiState.expensesByCategory.map { (category, amount) ->
                        PieChartEntry(amount.toFloat(), getRandomColor(), category)
                    }
                    SimplePieChart(entries = pieChartData, chartSize = 200f)
                } else {
                    Text("Nenhum gasto por categoria para exibir.")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Monthly Expenses Bar Chart
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Despesas do Mês Atual", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (uiState.monthlyExpenses.isNotEmpty()) {
                    BarChart(data = uiState.monthlyExpenses)
                } else {
                    Text("Nenhuma despesa mensal para exibir.")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Despesas do Mês Anterior", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (uiState.previousMonthlyExpenses.isNotEmpty()) {
                    BarChart(data = uiState.previousMonthlyExpenses)
                } else {
                    Text("Nenhuma despesa do mês anterior para exibir.")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Monthly Income Bar Chart
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Receita do Mês Atual", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (uiState.monthlyIncome.isNotEmpty()) {
                    BarChart(data = uiState.monthlyIncome, barColor = MaterialTheme.colorScheme.secondary)
                } else {
                    Text("Nenhuma receita mensal para exibir.")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Receita do Mês Anterior", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (uiState.previousMonthlyIncome.isNotEmpty()) {
                    BarChart(data = uiState.previousMonthlyIncome, barColor = MaterialTheme.colorScheme.secondary)
                } else {
                    Text("Nenhuma receita do mês anterior para exibir.")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Projections Placeholder
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Projeções Futuras", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Funcionalidade de projeções futuras será implementada aqui.")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Filters Placeholder
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Filtros", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Opções de filtro serão implementadas aqui.")
            }
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
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .width(chartSize.dp)
            .height(chartSize.dp)
            .onSizeChanged { canvasSize = it },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = canvasSize.width.toFloat()
            val canvasHeight = canvasSize.height.toFloat()
            val radius = with(this) { chartSize.dp.toPx() } / 2f
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
                drawIntoCanvas {
                    val currentCanvasWidth = canvasSize.width.toFloat()
                    val currentCanvasHeight = canvasSize.height.toFloat()
                    val currentRadius = chartSize.dp.toPx() / 2f
                    val currentCenter = Offset(currentCanvasWidth / 2f, currentCanvasHeight / 2f)
                    val labelRadius = currentRadius * 0.7f // Position labels slightly inside the radius
                    it.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = with(this) { 12.sp.toPx() }
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        drawText(
                            "${(entry.value / totalValue * 100).toInt()}%",
                            currentCenter.x + labelRadius * cos((startAngle + sweepAngle / 2).toDouble() * Math.PI / 180.0).toFloat(),
                            currentCenter.y + labelRadius * sin((startAngle + sweepAngle / 2).toDouble() * Math.PI / 180.0).toFloat() + paint.textSize / 3, // Adjust y for vertical centering
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

fun getRandomColor(): Color {
    return Color(
        red = (0..255).random(),
        green = (0..255).random(),
        blue = (0..255).random()
    )
}
