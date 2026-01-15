package tj.msu.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tj.msu.presentation.theme.MsuBlue
import tj.msu.presentation.theme.TextPractice

@Composable
fun DaySelector(
    selectedDayIndex: Int,
    displayedDates: List<String> = emptyList(),
    hasLessons: List<Boolean> = List(7) { true },
    onDaySelected: (Int) -> Unit
) {
    val days = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEachIndexed { index, dayName ->
            val isSelected = index == selectedDayIndex
            val dayHasLessons = hasLessons.getOrElse(index) { true }
            
            val backgroundColor = if (isSelected) MsuBlue else Color.Transparent
            
            val contentColor = when {
                isSelected -> Color.White
                !dayHasLessons -> TextPractice.copy(alpha = 0.6f)
                else -> Color.Gray
            }

            val textToShow = if (isSelected) {
                if (index < displayedDates.size) {

                    try {
                        val dateStr = displayedDates[index]

                        val parsed = dateStr.substringAfterLast("-", dateStr)
                         if (parsed.isBlank()) dayName else parsed
                    } catch (e: Exception) {
                        dayName
                    }
                } else {
                    dayName
                }
            } else {
                dayName
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor)
                    .clickable { onDaySelected(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = textToShow,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}