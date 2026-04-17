package com.example.myapplication.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProgrammerBitKeyboard(
    value: Long,
    wordSize: Int,
    onToggleBit: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalBits = 64
    val bitsPerRow = 16
    val rowCount = totalBits / bitsPerRow
    val activeValue = when (wordSize) {
        8 -> value and 0xFFL
        16 -> value and 0xFFFFL
        32 -> value and 0xFFFFFFFFL
        else -> value
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        for (row in 0 until rowCount) {
            val rowHigh = totalBits - 1 - row * bitsPerRow
            val rowLow = rowHigh - bitsPerRow + 1
            val groupHighs = buildList {
                var current = rowHigh
                while (current >= rowLow) {
                    add(current)
                    current -= 4
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupHighs.forEach { groupHigh ->
                    val groupLow = groupHigh - 3

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentWidth(Alignment.CenterHorizontally),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            for (bitIndex in groupHigh downTo groupLow) {
                                val isEnabled = bitIndex < wordSize
                                val isOn = ((activeValue ushr bitIndex) and 1L) == 1L
                                Text(
                                    text = if (isOn) "1" else "0",
                                    fontSize = 22.sp,
                                    color = when {
                                        !isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                        isOn -> Color(0, 101, 179)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = if (isEnabled) {
                                        Modifier.clickable { onToggleBit(bitIndex) }
                                    } else {
                                        Modifier
                                    }
                                )
                            }
                        }
                        Text(
                            text = groupLow.toString(),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
