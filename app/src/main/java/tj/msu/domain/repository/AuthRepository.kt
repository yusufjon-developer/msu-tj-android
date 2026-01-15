package tj.msu.domain.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import tj.msu.data.model.UserProfileDto

interface AuthRepository {
    val currentUser: FirebaseUser?

    val authState: Flow<FirebaseUser?>

    suspend fun signInWithEmail(email: String, pass: String): Result<Unit>
    suspend fun signUpWithEmail(
        email: String, 
        pass: String, 
        surname: String, 
        firstName: String, 
        patronymic: String, 
        role: String, 
        faculty: String, 
        course: Int
    ): Result<Unit>
    suspend fun signInWithGoogle(idToken: String): Result<Unit>

    suspend fun saveUserProfile(
        uid: String, 
        surname: String, 
        firstName: String, 
        patronymic: String, 
        role: String, 
        faculty: String, 
        course: Int
    ): Result<Unit>
    suspend fun getUserProfile(uid: String): Result<UserProfileDto?>
    suspend fun saveFcmToken()

    fun signOut()

    fun subscribeToGroupNotifications(faculty: String, course: Int)

    fun unsubscribeFromGroupNotifications(faculty: String, course: Int)

    suspend fun getAppInfo(): Result<tj.msu.domain.model.AppInfo>
}