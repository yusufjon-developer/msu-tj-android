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

                    if (newGroupId != currentGroupId || currentState.scheduleByDay.isEmpty()) {

                        setState {
                            copy(
                                isLoading = currentState.scheduleByDay.isEmpty(),
                                selectedFacultyCode = localProfile.facultyCode,
                                selectedCourse = localProfile.course
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

    private fun loadSchedule(groupId: String, isNextWeek: Boolean = false) {
        viewModelScope.launch {

            launch {
                repository.checkNextWeekScheduleAvailability(groupId).collect { available ->
                    setState { copy(isNextWeekAvailable = available) }

                }
            }

            repository.getDailySchedule(groupId, isNextWeek)
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
}