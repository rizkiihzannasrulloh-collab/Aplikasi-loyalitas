package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NasiCream
import com.example.ui.theme.NasiPurple

@Composable
fun CheckerboardBorder(
    modifier: Modifier = Modifier,
    columns: Int = 2,
    squareSize: Dp = 10.dp,
    color1: Color = NasiPurple,
    color2: Color = Color.White
) {
    Box(
        modifier = modifier
            .width(squareSize * columns)
            .fillMaxHeight()
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val sqPx = squareSize.toPx()
            val totalRows = (size.height / sqPx).toInt() + 1

            for (row in 0..totalRows) {
                for (col in 0 until columns) {
                    val isColor1 = (row + col) % 2 == 0
                    drawRect(
                        color = if (isColor1) color1 else color2,
                        topLeft = Offset(col * sqPx, row * sqPx),
                        size = Size(sqPx, sqPx)
                    )
                }
            }
        }
    }
}
