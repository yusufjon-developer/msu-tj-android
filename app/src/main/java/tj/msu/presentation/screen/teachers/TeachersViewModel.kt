

package tj.msu.presentation.screen.teachers

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import tj.msu.data.repository.UserPreferencesRepository
import tj.msu.domain.model.FreeRooms
import tj.msu.domain.model.LessonType
import tj.msu.domain.model.TeacherModel
import tj.msu.domain.repository.TeacherRepository
import tj.msu.presentation.core.base.MVIViewModel
import tj.msu.presentation.util.DateUtils

@KoinViewModel
class TeachersViewModel(
    private val repository: TeacherRepository,
    private val scheduleRepository: tj.msu.domain.repository.ScheduleRepository,
    private val userPrefs: UserPreferencesRepository
) : MVIViewModel<TeachersEvent, TeachersEffect, TeachersState>() {

    override fun createInitialState() = TeachersState()

    init {
        viewModelScope.launch {
            repository.checkNextWeekTeachersAvailability().collect { available ->
                setState { copy(isNextWeekAvailable = available) }
            }
        }
        observeSettings()
        setEvent(TeachersEvent.LoadData)
    }
    
    private fun observeSettings() {
        viewModelScope.launch {
             userPrefs.userProfile.collect { profile ->
                 if (profile != null) {
                     val isSmart = profile.isSmartFreeRooms
                     val isExpandable = profile.isExpandableFreeRooms
                     
                     var targetName: String? = null
                     if (profile.role == "teacher") {
                         fun getInitial(name: String): String {
                             if (name.isBlank()) return ""
                             val trimmed = name.trim()
                             if (trimmed.length >= 2 && trimmed.substring(0, 2).equals("Дж", ignoreCase = true)) {
                                 return "Дж."
                             }
                             return trimmed.first().toString() + "."
                         }

                         val f = getInitial(profile.firstName)
                         val p = getInitial(profile.patronymic)
                         targetName = "${profile.surname} $f$p".trim()
                     }

                     if (isSmart != currentState.isSmartFreeRooms || 
                         isExpandable != currentState.isExpandableFreeRooms ||
                         targetName != currentState.targetTeacherName
                     ) {
                         val smartChanged = isSmart != currentState.isSmartFreeRooms
                         val targetChanged = targetName != currentState.targetTeacherName
                         
                         setState { copy(
                             isSmartFreeRooms = isSmart, 
                             isExpandableFreeRooms = isExpandable,
                             targetTeacherName = targetName
                         ) }
                         
                         if (smartChanged || targetChanged) loadTeachers()
                     }
                 }
             }
        }
    }

    override fun handleEvent(event: TeachersEvent) {
        when (event) {
            is TeachersEvent.LoadData -> loadTeachers()
            is TeachersEvent.OnSearch -> filterTeachers(event.query)
            is TeachersEvent.OnSelectTeacher -> setState {
                copy(selectedTeacher = event.teacher)
            }
            is TeachersEvent.OnResetSearch -> {
                setState {
                    copy(searchQuery = "", filteredTeachers = allTeachers)
                }
            }
            is TeachersEvent.OnToggleNextWeek -> {
                val nextWeek = !currentState.isNextWeek
                setState { copy(isNextWeek = nextWeek, selectedTeacher = null) }
                loadTeachers()
            }
        }
    }

    private var loadJob: kotlinx.coroutines.Job? = null

    private fun loadTeachers() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
             setState { copy(isLoading = true) }
             
             try {
                val teachersFlow = repository.getTeachers(currentState.isNextWeek)
                val freeRoomsFlow = scheduleRepository.getFreeRooms(currentState.isNextWeek)
                
                combine(teachersFlow, freeRoomsFlow) { teachers: List<TeacherModel>, freeRooms: FreeRooms ->
                     if (currentState.isSmartFreeRooms) {
                         processTeachersWithFreeRooms(teachers, freeRooms.schedule)
                     } else {
                         teachers
                     }
                }
                .catch { error ->
                    setState { copy(isLoading = false) }
                    setEffect { TeachersEffect.ShowError(error.message ?: "Ошибка загрузки") }
                }
                .collect { incomingTeachers ->
                    setState {
                        val target = currentState.targetTeacherName
                        
                        val updatedSelectedTeacher = if (target != null) {
                            incomingTeachers.find { it.name.trim().equals(target, ignoreCase = true) }
                                ?: TeacherModel(id = "fake", name = target, days = emptyList())
                        } else {
                            if (selectedTeacher != null) {
                                incomingTeachers.find { it.id == selectedTeacher.id }
                                    ?: incomingTeachers.firstOrNull()
                            } else {
                                incomingTeachers.firstOrNull()
                            }
                        }
    
                        copy(
                            isLoading = false,
                            allTeachers = incomingTeachers,
                            filteredTeachers = performFilter(incomingTeachers, searchQuery),
                            selectedTeacher = updatedSelectedTeacher,
                            weekDates = DateUtils.getWeekDates(currentState.isNextWeek)
                        )
                    }
                }
             } catch (e: Exception) {
                 e.printStackTrace()
                 setState { copy(isLoading = false) }
             }
        }
    }
    
    private fun processTeachersWithFreeRooms(
        teachers: List<TeacherModel>,
        freeRoomsSchedule: Map<String, Map<String, List<String>>>
    ): List<TeacherModel> {
        return teachers.map { teacher ->
            val updatedDays = teacher.days.map { day ->
                val dayLessons = day.lessons
                if (dayLessons.isEmpty()) return@map day

                val activeIndices = dayLessons.mapIndexedNotNull { index, lesson ->
                    if (lesson.type != LessonType.WINDOW) index + 1 else null
                }
                
                val minPair = activeIndices.minOrNull() ?: -1
                val maxPair = activeIndices.maxOrNull() ?: -1
                
                val pairsToShow = mutableSetOf<Int>()
                if (minPair != -1) {
                    if (minPair > 1) pairsToShow.add(minPair - 1)
                    if (maxPair < 5) pairsToShow.add(maxPair + 1)
                    for (p in (minPair + 1) until maxPair) {
                        if (!activeIndices.contains(p)) pairsToShow.add(p)
                    }
                }
                
                val mergedLessons = dayLessons.mapIndexed { index, lesson ->
                    val pairNum = index + 1
                    if (pairsToShow.contains(pairNum) && lesson.type == LessonType.WINDOW) {
                        val dayKey = (day.dayIndex + 1).toString()
                        val pairKey = pairNum.toString()
                        val rooms = freeRoomsSchedule[dayKey]?.get(pairKey) ?: emptyList()
                        lesson.copy(freeRooms = rooms)
                    } else {
                        lesson
                    }
                }
                
                day.copy(lessons = mergedLessons)
            }
            teacher.copy(days = updatedDays)
        }
    }

    private fun filterTeachers(query: String) {
        setState {
            copy(
                searchQuery = query,
                filteredTeachers = performFilter(allTeachers, query)
            )
        }
    }


    private fun performFilter(list: List<TeacherModel>, query: String): List<TeacherModel> {
        if (query.isBlank()) return list
        return list.filter { it.name.contains(query, ignoreCase = true) }
    }
}