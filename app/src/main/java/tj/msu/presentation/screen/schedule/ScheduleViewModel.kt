package tj.msu.presentation.screen.schedule

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import tj.msu.domain.repository.ScheduleRepository
import tj.msu.presentation.core.base.MVIViewModel

@KoinViewModel
class ScheduleViewModel(
    private val repository: ScheduleRepository
) : MVIViewModel<ScheduleEvent, ScheduleEffect, ScheduleState>() {

    override fun createInitialState(): ScheduleState = ScheduleState()

    init {
        loadSchedule(generateGroupId(currentState.selectedFacultyCode, currentState.selectedCourse))
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
        }
    }

    private fun generateGroupId(faculty: String, course: Int): String {
        return "${faculty}_${course}"
    }

    private fun loadSchedule(groupId: String) {
        viewModelScope.launch {
            repository.getDailySchedule(groupId)
                .onStart {
                    setState { copy(isLoading = true, error = null) }
                }
                .catch { e ->
                    setState { copy(isLoading = false, error = e.message) }
                    setEffect { ScheduleEffect.ShowToast("Ошибка: ${e.message}") }
                }
                .collect { lessons ->
                    val grouped = lessons.groupBy { it.dayIndex }

                    setState {
                        copy(
                            isLoading = false,
                            scheduleByDay = grouped
                        )
                    }
                }
        }
    }
}