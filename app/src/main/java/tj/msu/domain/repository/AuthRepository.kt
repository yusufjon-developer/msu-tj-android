package tj.msu.domain.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import tj.msu.data.model.UserProfileDto

interface AuthRepository {
    val currentUser: FirebaseUser?

    val authState: Flow<FirebaseUser?>

    suspend fun signInWithEmail(email: String, pass: String): Result<Unit>
    suspend fun signUpWithEmail(email: String, pass: String, name: String, faculty: String, course: Int): Result<Unit>
    suspend fun signInWithGoogle(idToken: String): Result<Unit>

    suspend fun saveUserProfile(uid: String, name: String, faculty: String, course: Int): Result<Unit>
    suspend fun getUserProfile(uid: String): Result<UserProfileDto?>

    fun signOut()
}