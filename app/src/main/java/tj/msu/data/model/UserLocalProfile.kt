package tj.msu.data.model

data class UserLocalProfile(
    val name: String = "",
    val facultyCode: String = "",
    val course: Int = 0,
    val isExpandableFreeRooms: Boolean = true
)