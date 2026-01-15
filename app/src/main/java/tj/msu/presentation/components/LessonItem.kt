package tj.msu.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tj.msu.domain.model.Lesson
import tj.msu.domain.model.LessonType
import tj.msu.presentation.theme.*

@Composable
fun LessonItem(
    lesson: Lesson,
    isExpandable: Boolean = false,
    onClick: () -> Unit
) {
    if (lesson.type == LessonType.WINDOW) {
        WindowItem(lesson, isExpandable)
    } else {
        StandardLessonItem(lesson, onClick)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WindowItem(lesson: Lesson, isExpandable: Boolean) {
    val hasFreeRooms = !lesson.freeRooms.isNullOrEmpty()
    var isExpanded by remember(lesson.id) { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .let { if (isExpandable && hasFreeRooms) it.animateContentSize().clickable { isExpanded = !isExpanded } else it },
        colors = CardDefaults.cardColors(
            containerColor = if (hasFreeRooms) CardBackground else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (hasFreeRooms) 2.dp else 0.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(if (hasFreeRooms) MsuBlue else Color.LightGray)
            )

            Column(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .width(75.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val times = lesson.time.split("\n")
                Text(
                    text = times.getOrNull(0) ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = times.getOrNull(1) ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                if (hasFreeRooms) {
                    Text(
                        text = "Свободные аудитории:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxLines = if (!isExpandable || isExpanded) Int.MAX_VALUE else 1,
                        overflow = FlowRowOverflow.expandIndicator {
                             if (isExpandable) {
                                 Box(
                                     modifier = Modifier
                                         .height(32.dp)
                                         .wrapContentWidth(),
                                     contentAlignment = Alignment.Center
                                 ) {
                                     Icon(
                                         imageVector = Icons.Default.KeyboardArrowDown,
                                         contentDescription = "Expand",
                                         tint = Color.Gray
                                     )
                                 }
                             }
                        }
                    ) {
                        lesson.freeRooms?.forEach { room ->
                            RoomChip(room, isLarge = isExpandable)
                        }
                        
                        if (isExpandable && isExpanded) {
                            Box(
                                modifier = Modifier
                                    .height(32.dp)
                                    .wrapContentWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Collapse",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Свободно",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun RoomChip(room: String, isLarge: Boolean) {
    Box(
        modifier = Modifier
            .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(8.dp))
            .background(Color(0xFFF1F8E9), RoundedCornerShape(8.dp))
            .padding(
                horizontal = if (isLarge) 12.dp else 8.dp,
                vertical = if (isLarge) 6.dp else 4.dp
            )
    ) {
        Text(
            text = room,
            style = if (isLarge) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
    }
}

@Composable
fun StandardLessonItem(
    lesson: Lesson,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(MsuBlue)
            )

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(75.dp)
                    .background(MsuBackground)
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val times = lesson.time.split("\n")
                Text(
                    text = times.getOrNull(0) ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = times.getOrNull(1) ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                LessonTypeBadge(lesson.type)

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (lesson.room.isNotBlank()) {
                    InfoRow(icon = Icons.Default.LocationOn, text = lesson.room)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (lesson.teacher.isNotBlank()) {
                    InfoRow(icon = Icons.Default.Person, text = lesson.teacher)
                }
            }
        }
    }
}

@Composable
fun LessonTypeBadge(type: LessonType) {
    val (bgColor, textColor) = when (type) {
        LessonType.LECTURE -> BadgeLecture to TextLecture
        LessonType.PRACTICE -> BadgePractice to TextPractice
        LessonType.SEMINAR -> BadgeSeminar to TextSeminar
        LessonType.LAB -> BadgeLab to TextLab
        LessonType.EXAM -> BadgeExam to TextExam
        LessonType.CREDIT -> BadgeCredit to TextCredit
        LessonType.CONSULTATION -> BadgeConsultation to TextConsultation
        LessonType.STREAM -> BadgeStream to TextStream
        else -> Color(0xFFEEEEEE) to Color.Gray
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = type.displayName.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = 10.sp
        )
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MsuBlue
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            fontSize = 13.sp
        )
    }
}