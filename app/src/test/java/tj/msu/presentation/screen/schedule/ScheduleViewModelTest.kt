package tj.msu.presentation.screen.schedule

import app.cash.turbine.test
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import tj.msu.CoroutinesTestExtension
import tj.msu.data.model.UserLocalProfile
import tj.msu.data.repository.UserPreferencesRepository
import tj.msu.domain.model.FreeRooms
import tj.msu.domain.model.Lesson
import tj.msu.domain.model.LessonType
import tj.msu.domain.repository.ScheduleRepository

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModelTest {

    companion object {
        @JvmField
        @RegisterExtension
        val coroutinesExtension = CoroutinesTestExtension()
    }

    private val repository: ScheduleRepository = mockk(relaxed = true)
    private val userPrefs: UserPreferencesRepository = mockk(relaxed = true)

    private val testProfile = UserLocalProfile(
        name = "Иван Иванов",
        surname = "Иванов",
        firstName = "Иван",
        patronymic = "Иванович",
        role = "student",
        facultyCode = "pmi",
        course = 3,
        isExpandableFreeRooms = true,
        isSmartFreeRooms = false
    )

    private val testLessons = listOf(
        Lesson(
            id = "Mon_1",
            title = "Математический анализ",
            time = "08:00\n09:30",
            type = LessonType.LECTURE,
            teacher = "Петров П.П.",
            room = "301",
            dayIndex = 0
        )
    )

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        every { userPrefs.userProfile } returns flowOf(testProfile)
        every { repository.checkNextWeekScheduleAvailability(any()) } returns flowOf(false)
        every { repository.getDailySchedule(any(), any()) } returns flowOf(testLessons)
        every { repository.getFreeRooms(any()) } returns flowOf(FreeRooms())
    }

    @Test
    fun `load schedule successfully when profile is observed`() = runTest {
        val viewModel = ScheduleViewModel(repository, userPrefs)

        viewModel.uiState.test {
            // Initial state (or state after loaded due to flowOf emission)
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals("pmi", state.selectedFacultyCode)
            assertEquals(3, state.selectedCourse)
            assertEquals(1, state.scheduleByDay[0]?.size)
            assertEquals("Математический анализ", state.scheduleByDay[0]?.first()?.title)
        }

        verify(exactly = 1) { repository.getDailySchedule("pmi_3", false) }
    }

    @Test
    fun `error state is emitted when repository throws exception`() = runTest {
        val errorMessage = "Network failure"
        every { repository.getDailySchedule(any(), any()) } returns flow {
            throw Exception(errorMessage)
        }

        val viewModel = ScheduleViewModel(repository, userPrefs)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(errorMessage, state.error)
        }
    }

    @Test
    fun `toggle next week triggers loading with next week parameter`() = runTest {
        val viewModel = ScheduleViewModel(repository, userPrefs)

        viewModel.uiState.test {
            // Skip initial loaded state
            var state = awaitItem()
            assertFalse(state.isNextWeek)

            viewModel.setEvent(ScheduleEvent.OnToggleNextWeek)

            state = awaitItem()
            assertTrue(state.isNextWeek)
        }

        verify(exactly = 1) { repository.getDailySchedule("pmi_3", true) }
    }

    @Test
    fun `applying filters updates state and reloads schedule`() = runTest {
        val viewModel = ScheduleViewModel(repository, userPrefs)

        viewModel.uiState.test {
            var state = awaitItem()
            assertEquals("pmi", state.selectedFacultyCode)
            assertEquals(3, state.selectedCourse)

            viewModel.setEvent(ScheduleEvent.OnApplyFilters("geo", 4))

            state = awaitItem()
            assertEquals("geo", state.selectedFacultyCode)
            assertEquals(4, state.selectedCourse)
        }

        verify(exactly = 1) { repository.getDailySchedule("geo_4", false) }
    }
}
