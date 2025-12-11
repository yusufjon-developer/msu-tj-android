package tj.msu.presentation.screen.freerooms

import tj.msu.presentation.core.base.UiEffect
import tj.msu.presentation.core.base.UiEvent
import tj.msu.presentation.core.base.UiState

data class PairFreeRooms(
    val pairNumber: Int,
    val time: String,
    val freeRooms: List<String>
)

data class FreeRoomsState(
    val isLoading: Boolean = false,
    val freeRoomsByDay: Map<Int, List<PairFreeRooms>> = emptyMap(),
    val currentDayIndex: Int = 0,
    val isExpandableLayout: Boolean = true,
    val error: String? = null
) : UiState

sealed interface FreeRoomsEvent : UiEvent {
    data object LoadData : FreeRoomsEvent
    data class SelectDay(val dayIndex: Int) : FreeRoomsEvent
}

sealed interface FreeRoomsEffect : UiEffect