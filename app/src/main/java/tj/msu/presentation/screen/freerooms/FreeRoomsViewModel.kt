package tj.msu.presentation.screen.freerooms

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import tj.msu.data.repository.UserPreferencesRepository
import tj.msu.domain.repository.ScheduleRepository
import tj.msu.presentation.core.base.MVIViewModel
import tj.msu.presentation.util.DateUtils
import java.time.LocalDate

@KoinViewModel
class FreeRoomsViewModel(
    private val repository: ScheduleRepository,
    private val userPrefs: UserPreferencesRepository
) : MVIViewModel<FreeRoomsEvent, FreeRoomsEffect, FreeRoomsState>() {

    override fun createInitialState(): FreeRoomsState {
        val today = LocalDate.now().dayOfWeek.value - 1
        val safeDay = if (today in 0..6) today else 0
        return FreeRoomsState(currentDayIndex = safeDay)
    }

    init {
        setEvent(FreeRoomsEvent.LoadData)
        observeSettings()
        
        viewModelScope.launch {
            repository.checkNextWeekFreeRoomsAvailability().collect { available ->
                setState { copy(isNextWeekAvailable = available) }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            userPrefs.userProfile.collect { profile ->
                if (profile != null) {
                    val needReload = profile.isSmartFreeRooms != currentState.isSmartFreeRooms ||
                            profile.facultyCode != currentState.selectedFaculty ||
                            profile.course != currentState.selectedCourse

                    setState {
                        copy(
                            isExpandableLayout = profile.isExpandableFreeRooms,
                            isSmartFreeRooms = profile.isSmartFreeRooms,
                            selectedFaculty = profile.facultyCode,
                            selectedCourse = profile.course
                        )
                    }

                    if (needReload) {
                        loadData()
                    }
                }
            }
        }
    }

    override fun handleEvent(event: FreeRoomsEvent) {
        when (event) {
            is FreeRoomsEvent.LoadData -> loadData()
            is FreeRoomsEvent.SelectDay -> {
                setState { copy(currentDayIndex = event.dayIndex) }
            }
            is FreeRoomsEvent.OnToggleNextWeek -> {
                val nextWeek = !currentState.isNextWeek
                setState { copy(isNextWeek = nextWeek) }
                loadData()
            }
        }
    }

    private var dataJob: Job? = null
    private fun loadData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            repository.getFreeRooms(currentState.isNextWeek)
                .onStart { setState { copy(isLoading = true) } }
                .catch { e -> setState { copy(isLoading = false, error = e.message) } }
                .collect { freeRooms ->
                    withContext(Dispatchers.Default) {
                        val processedMap = processAllDays(freeRooms.schedule)
                        val dates = DateUtils.getWeekDates(currentState.isNextWeek)

                        setState {
                            copy(
                                isLoading = false,
                                freeRoomsByDay = processedMap,
                                weekDates = dates
                            )
                        }
                    }
                }
        }
    }

    private suspend fun processAllDays(
        rawData: Map<String, Map<String, List<String>>>
    ): Map<Int, List<PairFreeRooms>> = withContext(Dispatchers.Default) {
        val resultMap = mutableMapOf<Int, List<PairFreeRooms>>()

        for (dayIdx in 0..6) {
            val dayKey = (dayIdx + 1).toString()
            val daySchedule = rawData[dayKey] ?: emptyMap()
            val pairsList = mutableListOf<PairFreeRooms>()

            for (i in 1..5) {
                val pairKey = i.toString()
                val rooms = daySchedule[pairKey] ?: emptyList()

                pairsList.add(
                    PairFreeRooms(
                        pairNumber = i,
                        time = getPairTime(i),
                        freeRooms = rooms.sorted()
                    )
                )
            }
            resultMap[dayIdx] = pairsList
        }
        return@withContext resultMap
    }

    private fun getPairTime(pairNum: Int): String {
        return when (pairNum) {
            1 -> "08:00\n09:30"
            2 -> "09:45\n11:15"
            3 -> "11:30\n13:00"
            4 -> "14:00\n15:30"
            5 -> "15:45\n17:15"
            else -> ""
        }
    }
}