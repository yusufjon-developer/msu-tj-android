

package tj.msu.presentation.screen.teachers

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.koin.android.annotation.KoinViewModel
import tj.msu.domain.model.TeacherModel
import tj.msu.domain.repository.TeacherRepository
import tj.msu.presentation.core.base.MVIViewModel

@KoinViewModel
class TeachersViewModel(
    private val repository: TeacherRepository
) : MVIViewModel<TeachersEvent, TeachersEffect, TeachersState>() {

    override fun createInitialState() = TeachersState()

    init {

        setEvent(TeachersEvent.LoadData)
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
        }
    }

    private fun loadTeachers() {
        repository.getTeachers()
            .onStart { setState { copy(isLoading = true) } }
            .catch { error ->
                setState { copy(isLoading = false) }
                setEffect { TeachersEffect.ShowError(error.message ?: "Ошибка загрузки") }
            }
            .onEach { incomingTeachers ->
                setState {

                    val updatedSelectedTeacher = if (selectedTeacher != null) {
                        incomingTeachers.find { it.id == selectedTeacher.id }

                            ?: incomingTeachers.firstOrNull()
                    } else {

                        incomingTeachers.firstOrNull()
                    }

                    copy(
                        isLoading = false,
                        allTeachers = incomingTeachers,

                        filteredTeachers = performFilter(incomingTeachers, searchQuery),

                        selectedTeacher = updatedSelectedTeacher
                    )
                }
            }
            .launchIn(viewModelScope)
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