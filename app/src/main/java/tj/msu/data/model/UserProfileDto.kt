package tj.msu.data.model

data class UserProfileDto(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val facultyCode: String = "",
    val course: Int = 1
)