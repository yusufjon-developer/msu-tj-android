package tj.msu.data.model

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName

@IgnoreExtraProperties
data class TeacherDto(
    val name: String? = null,
    @get:PropertyName("updated_at")
    val updated_at: String? = null,
    val days: Any? = null
)