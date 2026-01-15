package tj.msu.data.model

data class UserLocalProfile(
    val name: String = "",
    val surname: String = "",
    val firstName: String = "",
    val patronymic: String = "",
    val role: String = "student",
    val facultyCode: String = "",
    val course: Int = 0,
    val isExpandableFreeRooms: Boolean = true,
    val isSmartFreeRooms: Boolean = false
)