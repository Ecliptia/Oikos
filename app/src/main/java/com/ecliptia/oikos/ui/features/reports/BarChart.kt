package com.ecliptia.oikos.ui.features.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BarChart(
    data: Map<String, Double>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    labelColor: Color = MaterialTheme.colorScheme.onSurface
) {
    if (data.isEmpty()) {
        Text("Nenhum dado para exibir.", style = MaterialTheme.typography.bodyMedium)
        return
    }

    val maxValue = data.values.maxOrNull() ?: 0.0
    val barWidth = 30.dp
    val spaceBetweenBars = 16.dp

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("pt").setRegion("BR").build())

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .height(200.dp)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { (label, value) ->
                val barHeight = if (maxValue > 0) {
                    with(LocalDensity.current) {
                        val pixelValue = (value / maxValue).toFloat() * 150.dp.toPx()
                        pixelValue.toDp()
                    }
                } else 0.dp // Max bar height 150.dp
                Column(
                    modifier = Modifier
                        .width(barWidth)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Value label above bar
                    Text(
                        text = currencyFormat.format(value),
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Bar
                    Canvas(modifier = Modifier.width(barWidth).height(barHeight)) {
                        drawRect(color = barColor, size = Size(size.width, size.height))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    // Category label below bar
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.width(spaceBetweenBars))
            }
        }
    }
}
