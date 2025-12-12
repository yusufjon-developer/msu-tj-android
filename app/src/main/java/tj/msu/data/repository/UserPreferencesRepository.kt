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
        val FACULTY = stringPreferencesKey("user_faculty")
        val COURSE = intPreferencesKey("user_course")
        val IS_EXPANDABLE_FREE_ROOMS = booleanPreferencesKey("is_expandable_free_rooms")
    }

    val userProfile: Flow<UserLocalProfile?> = dataStore.data.map { prefs ->
        val faculty = prefs[Keys.FACULTY]
        val course = prefs[Keys.COURSE]
        val name = prefs[Keys.NAME]
        val isExpandable = prefs[Keys.IS_EXPANDABLE_FREE_ROOMS] ?: true

        if (faculty != null && course != null) {
            UserLocalProfile(
                name = name ?: "",
                facultyCode = faculty,
                course = course,
                isExpandableFreeRooms = isExpandable
            )
        } else {
            null
        }
    }

    suspend fun saveUserSelection(name: String, faculty: String, course: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.NAME] = name
            prefs[Keys.FACULTY] = faculty
            prefs[Keys.COURSE] = course
        }
    }

    suspend fun setFreeRoomsLayout(isExpandable: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.IS_EXPANDABLE_FREE_ROOMS] = isExpandable
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}