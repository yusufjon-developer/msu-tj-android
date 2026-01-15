package tj.msu.presentation.screen.schedule

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import tj.msu.data.repository.UserPreferencesRepository
import tj.msu.domain.repository.ScheduleRepository
import tj.msu.presentation.core.base.MVIViewModel

@KoinViewModel
class ScheduleViewModel(
    private val repository: ScheduleRepository,
    private val userPrefs: UserPreferencesRepository
) : MVIViewModel<ScheduleEvent, ScheduleEffect, ScheduleState>() {

    override fun createInitialState(): ScheduleState = ScheduleState(isLoading = true)

    init {
        observeUserPreferences()
    }

    private fun observeUserPreferences() {
        viewModelScope.launch {
            userPrefs.userProfile.collectLatest { localProfile ->

                if (localProfile != null) {
                    val newGroupId = generateGroupId(localProfile.facultyCode, localProfile.course)
                    
                    val currentGroupId = generateGroupId(currentState.selectedFacultyCode, currentState.selectedCourse)
                    
                    val isSmartChanged = localProfile.isSmartFreeRooms != currentState.isSmartFreeRooms
                    val isExpandableChanged = localProfile.isExpandableFreeRooms != currentState.isExpandableFreeRooms
                    
                    if (newGroupId != currentGroupId || isSmartChanged || isExpandableChanged || currentState.scheduleByDay.isEmpty()) {

                        setState {
                            copy(
                                isLoading = currentState.scheduleByDay.isEmpty(),
                                selectedFacultyCode = localProfile.facultyCode,
                                selectedCourse = localProfile.course,
                                isSmartFreeRooms = localProfile.isSmartFreeRooms,
                                isExpandableFreeRooms = localProfile.isExpandableFreeRooms
                            )
                        }

                        loadSchedule(newGroupId)
                    }
                } else {
                    loadDefaultSchedule()
                }
            }
        }
    }

    private fun loadDefaultSchedule() {
        val defaultGroupId = generateGroupId(currentState.selectedFacultyCode, currentState.selectedCourse)
        loadSchedule(defaultGroupId)
    }

    override fun handleEvent(event: ScheduleEvent) {
        when (event) {
            is ScheduleEvent.LoadData -> {
                val groupId = generateGroupId(currentState.selectedFacultyCode, currentState.selectedCourse)
                loadSchedule(groupId)
            }
            is ScheduleEvent.OnApplyFilters -> {
                setState { copy(selectedFacultyCode = event.facultyCode, selectedCourse = event.course) }
                val groupId = generateGroupId(event.facultyCode, event.course)
                loadSchedule(groupId)
            }
            is ScheduleEvent.OnLessonClick -> {
                setEffect { ScheduleEffect.ShowToast(event.name) }
            }
            is ScheduleEvent.OnToggleNextWeek -> {
                val newNextWeek = !currentState.isNextWeek
                setState { copy(isNextWeek = newNextWeek) }
                val groupId = generateGroupId(currentState.selectedFacultyCode, currentState.selectedCourse)
                loadSchedule(groupId, newNextWeek)
            }
        }
    }

    private fun generateGroupId(faculty: String, course: Int): String {
        return "${faculty}_${course}"
    }

    private var currentJob: kotlinx.coroutines.Job? = null

    private fun loadSchedule(groupId: String, isNextWeek: Boolean = false) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {

            launch {
                repository.checkNextWeekScheduleAvailability(groupId).collect { available ->
                    setState { copy(isNextWeekAvailable = available) }
                }
            }
            
            val scheduleFlow = repository.getDailySchedule(groupId, isNextWeek)
            val freeRoomsFlow = repository.getFreeRooms(isNextWeek)
            
            kotlinx.coroutines.flow.combine(scheduleFlow, freeRoomsFlow) { lessons, freeRoomsData ->
                if (currentState.isSmartFreeRooms) {
                    mergeFreeRooms(lessons, freeRoomsData.schedule)
                } else {
                    lessons
                }
            }
                .onStart {
                    if (currentState.scheduleByDay.isEmpty()) {
                        setState { copy(isLoading = true, error = null) }
                    }
                }
                .catch { e ->
                    setState { copy(isLoading = false, error = e.message) }
                    setEffect { ScheduleEffect.ShowToast("Ошибка: ${e.message}") }
                }
                .collect { lessons ->
                    val grouped = withContext(Dispatchers.Default) {
                        lessons.groupBy { it.dayIndex }
                    }

                    val datesMap = mutableMapOf<Int, String>()
                    lessons.forEach { lesson ->
                        if (lesson.date != null) {
                            datesMap[lesson.dayIndex] = lesson.date
                        }
                    }
                    
                    val datesList = (0..6).map { index ->
                        datesMap[index] ?: ""
                    }

                    setState { 
                        copy(
                            isLoading = false, 
                            scheduleByDay = grouped, 
                            weekDates = datesList 
                        ) 
                    }
                }
        }
    }

    private fun mergeFreeRooms(
        lessons: List<tj.msu.domain.model.Lesson>,
        freeRoomsSchedule: Map<String, Map<String, List<String>>>
    ): List<tj.msu.domain.model.Lesson> {
        val lessonsByDay = lessons.groupBy { it.dayIndex }
        val result = mutableListOf<tj.msu.domain.model.Lesson>()

        for (dayIndex in 0..6) {
            val dayLessons = lessonsByDay[dayIndex] ?: emptyList()
            if (dayLessons.isEmpty()) continue
            
            val activeLessonIndices = dayLessons.mapIndexedNotNull { index, lesson ->
                if (lesson.type != tj.msu.domain.model.LessonType.WINDOW) index + 1 else null
            }
            
            val minPair = activeLessonIndices.minOrNull() ?: -1
            val maxPair = activeLessonIndices.maxOrNull() ?: -1

            val pairsToShowFreeRooms = mutableSetOf<Int>()
            
            if (minPair != -1) {
                if (minPair > 1) pairsToShowFreeRooms.add(minPair - 1)
                if (maxPair < 5) pairsToShowFreeRooms.add(maxPair + 1)

                for (p in (minPair + 1) until maxPair) {
                     if (!activeLessonIndices.contains(p)) {
                         pairsToShowFreeRooms.add(p)
                     }
                }
            } else {

            }

            dayLessons.forEachIndexed { i, lesson ->
                val pairNum = i + 1
                if (pairsToShowFreeRooms.contains(pairNum) && lesson.type == tj.msu.domain.model.LessonType.WINDOW) {
                    
                    val dayKey = (dayIndex + 1).toString()
                    val pairKey = pairNum.toString()
                    val rooms = freeRoomsSchedule[dayKey]?.get(pairKey) ?: emptyList()

                    result.add(lesson.copy(freeRooms = rooms))
                } else {
                    result.add(lesson)
                }
            }
        }
        return result
    }
}