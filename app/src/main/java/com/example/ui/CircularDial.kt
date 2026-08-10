package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OutlineColor
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextMain

@Composable
fun CircularDial(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val angleRange = 300f
    val startAngle = 120f
    val sweepAngle = angleRange
    
    // Normalized value 0f..1f
    val normalizedValue = (value - range.start) / (range.endInclusive - range.start)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.padding(8.dp)) {
        Text(label, color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(120.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val dragFactor = dragAmount.x * 0.5f - dragAmount.y * 0.5f
                            val newNormalized = (normalizedValue + dragFactor / 100f).coerceIn(0f, 1f)
                            val newValue = range.start + newNormalized * (range.endInclusive - range.start)
                            onValueChange(newValue)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 10.dp.toPx()
                // Background arc
                drawArc(
                    color = OutlineColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                // Foreground arc
                drawArc(
                    color = PrimaryAccent,
                    startAngle = startAngle,
                    sweepAngle = (normalizedValue * angleRange),
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            // Text value inside dial
            val displayValue = if (range.start < 0f) {
                (normalizedValue * 200 - 100).toInt().toString()
            } else {
                (normalizedValue * 100).toInt().toString()
            }
            Text(displayValue, color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
