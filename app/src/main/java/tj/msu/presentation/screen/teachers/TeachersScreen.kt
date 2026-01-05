package tj.msu.presentation.screen.teachers

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import tj.msu.domain.model.TeacherModel
import tj.msu.presentation.components.DaySelector
import tj.msu.presentation.components.LessonItem
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TeachersScreen(
    viewModel: TeachersViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()


    val scope = rememberCoroutineScope()


    val currentDayOfWeek = remember {


        val day = LocalDate.now().dayOfWeek.value - 1
        if (day in 0..6) day else 0
    }


    val pagerState = rememberPagerState(
        initialPage = currentDayOfWeek,
        pageCount = { 7 }
    )


    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        TeacherSelectorCard(
            teacherName = state.selectedTeacher?.name ?: "Выберите преподавателя",
            onClick = {
                viewModel.setEvent(TeachersEvent.OnResetSearch)
                showBottomSheet = true
            }
        )

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val teacher = state.selectedTeacher
            if (teacher != null) {

                DaySelector(
                    selectedDayIndex = pagerState.currentPage,
                    onDaySelected = { index ->

                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )


                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),

                    ) { pageIndex ->


                    val daySchedule = teacher.days.find { it.dayIndex == pageIndex }
                    val lessons = daySchedule?.lessons ?: emptyList()

                    if (lessons.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(lessons) { lesson ->
                                LessonItem(
                                    lesson = lesson,
                                    onClick = { }
                                )
                            }
                        }
                    } else {

                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("В этот день занятий нет", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }


    if (showBottomSheet) {
        TeacherSearchBottomSheet(
            sheetState = sheetState,
            searchQuery = state.searchQuery,
            teachers = state.filteredTeachers,
            onSearch = { viewModel.setEvent(TeachersEvent.OnSearch(it)) },
            onSelect = { teacher ->
                viewModel.setEvent(TeachersEvent.OnSelectTeacher(teacher))
                showBottomSheet = false
            },
            onDismiss = { showBottomSheet = false }
        )
    }
}

@Composable
fun TeacherSelectorCard(
    teacherName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = teacherName.firstOrNull()?.uppercase() ?: "П",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1976D2),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))


            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Преподаватель",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = teacherName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1
                )
            }


            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select",
                tint = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherSearchBottomSheet(
    sheetState: SheetState,
    searchQuery: String,
    teachers: List<TeacherModel>,
    onSearch: (String) -> Unit,
    onSelect: (TeacherModel) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Выберите преподавателя",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )


            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearch,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Поиск по фамилии...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))


            LazyColumn(
                modifier = Modifier.fillMaxHeight(0.7f)
            ) {
                items(teachers) { teacher ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(teacher) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF5F5F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = teacher.name.take(1).uppercase(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = teacher.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )
                    }
                    HorizontalDivider(Modifier, thickness = 1.dp, color = Color(0xFFEEEEEE))
                }
            }
        }
    }
}