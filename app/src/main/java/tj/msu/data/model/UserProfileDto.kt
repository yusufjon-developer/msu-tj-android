package tj.msu.data.model

data class UserProfileDto(
    val id: String = "",
    val name: String = "",
    val surname: String = "",
    val firstName: String = "",
    val patronymic: String = "",
    val role: String = "student",
    val email: String = "",
    val facultyCode: String = "",
    val course: Int = 1,
    val subscribedTopics: List<String> = emptyList()
)