package tj.msu.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import tj.msu.data.model.UserLocalProfile

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

@Single
class UserPreferencesRepository(
    private val context: Context
) {
    private val dataStore = context.dataStore

    private object Keys {
        val NAME = stringPreferencesKey("user_name")
        val SURNAME = stringPreferencesKey("user_surname")
        val FIRST_NAME = stringPreferencesKey("user_first_name")
        val PATRONYMIC = stringPreferencesKey("user_patronymic")
        val ROLE = stringPreferencesKey("user_role")
        val FACULTY = stringPreferencesKey("user_faculty")
        val COURSE = intPreferencesKey("user_course")
        val IS_EXPANDABLE_FREE_ROOMS = booleanPreferencesKey("is_expandable_free_rooms")
        val IS_SMART_FREE_ROOMS = booleanPreferencesKey("is_smart_free_rooms")
    }

    val userProfile: Flow<UserLocalProfile?> = dataStore.data.map { prefs ->
        val faculty = prefs[Keys.FACULTY]
        val course = prefs[Keys.COURSE]
        val name = prefs[Keys.NAME] ?: ""
        val surname = prefs[Keys.SURNAME] ?: ""
        val firstName = prefs[Keys.FIRST_NAME] ?: ""
        val patronymic = prefs[Keys.PATRONYMIC] ?: ""
        val role = prefs[Keys.ROLE] ?: "student"
        
        val isExpandable = prefs[Keys.IS_EXPANDABLE_FREE_ROOMS] ?: true
        val isSmart = prefs[Keys.IS_SMART_FREE_ROOMS] ?: false

        if (name.isNotBlank() || (surname.isNotBlank() && firstName.isNotBlank())) {
            UserLocalProfile(
                name = name,
                surname = surname,
                firstName = firstName,
                patronymic = patronymic,
                role = role,
                facultyCode = faculty ?: "",
                course = course ?: 0,
                isExpandableFreeRooms = isExpandable,
                isSmartFreeRooms = isSmart
            )
        } else {
            null
        }
    }

    suspend fun saveUserSelection(
        name: String, 
        surname: String, 
        firstName: String, 
        patronymic: String, 
        role: String, 
        faculty: String, 
        course: Int
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.NAME] = name
            prefs[Keys.SURNAME] = surname
            prefs[Keys.FIRST_NAME] = firstName
            prefs[Keys.PATRONYMIC] = patronymic
            prefs[Keys.ROLE] = role
            prefs[Keys.FACULTY] = faculty
            prefs[Keys.COURSE] = course
        }
    }

    suspend fun setFreeRoomsLayout(isExpandable: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.IS_EXPANDABLE_FREE_ROOMS] = isExpandable
        }
    }

    suspend fun setSmartFreeRooms(isEnabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.IS_SMART_FREE_ROOMS] = isEnabled
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}