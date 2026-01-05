package tj.msu.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tj.msu.presentation.theme.MsuBlue
import java.time.LocalDate

@Composable
fun DaySelector(
    selectedDayIndex: Int,
    onDaySelected: (Int) -> Unit
) {
    val days = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")

    val currentWeekStart = remember {
        val today = LocalDate.now()
        today.minusDays((today.dayOfWeek.value - 1).toLong())
    }

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
            val backgroundColor = if (isSelected) MsuBlue else Color.Transparent
            val contentColor = if (isSelected) Color.White else Color.Gray

            val textToShow = if (isSelected) {
                val date = currentWeekStart.plusDays(index.toLong())
                date.dayOfMonth.toString()
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