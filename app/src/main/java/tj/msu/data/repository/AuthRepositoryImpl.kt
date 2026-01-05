package tj.msu.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.koin.core.annotation.Single
import tj.msu.data.model.UserProfileDto
import tj.msu.domain.repository.AuthRepository

@Single
class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val db: FirebaseDatabase,
    private val googleSignInClient: GoogleSignInClient,
    private val userPrefs: UserPreferencesRepository,
    private val firebaseMessaging: FirebaseMessaging,
    private val firebaseFirestore: FirebaseFirestore,
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithEmail(email: String, pass: String): Result<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        pass: String,
        name: String,
        faculty: String,
        course: Int
    ): Result<Unit> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: throw Exception("User ID not found")

            saveUserProfile(uid, name, faculty, course).getOrThrow()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveUserProfile(uid: String, name: String, faculty: String, course: Int): Result<Unit> {
        return try {
            val userProfile = UserProfileDto(id = uid, name = name, email = firebaseAuth.currentUser?.email ?: "", facultyCode = faculty, course = course)
            db.getReference("users").child(uid).setValue(userProfile).await()

            userPrefs.saveUserSelection(name, faculty, course)

            subscribeToGroupNotifications(faculty, course)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun subscribeToGroupNotifications(faculty: String, course: Int) {
        val groupTopic = "${faculty}_${course}"
        val globalTopic = "global"

        firebaseMessaging.subscribeToTopic(globalTopic)
        firebaseMessaging.subscribeToTopic(groupTopic)

        val user = firebaseAuth.currentUser ?: return

        val userRef = firebaseFirestore.collection("users").document(user.uid)

        userRef.update("subscribedTopics", FieldValue.arrayUnion(globalTopic, groupTopic))
            .addOnSuccessListener {
                android.util.Log.d("FCM", "Топики сохранены в профиль: $globalTopic, $groupTopic")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("FCM", "Ошибка сохранения топиков", e)
            }
    }

    override fun unsubscribeFromGroupNotifications(faculty: String, course: Int) {
        val groupTopic = "${faculty}_${course}"
        firebaseMessaging.unsubscribeFromTopic(groupTopic)

        val user = firebaseAuth.currentUser ?: return

        firebaseFirestore.collection("users").document(user.uid)
            .update("subscribedTopics", FieldValue.arrayRemove(groupTopic))
    }

    override suspend fun getUserProfile(uid: String): Result<UserProfileDto?> {
        return try {
            val snapshot = db.getReference("users").child(uid).get().await()
            val profile = snapshot.getValue(UserProfileDto::class.java)

            if (profile != null) {
                userPrefs.saveUserSelection(profile.name, profile.facultyCode, profile.course)
            }

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveFcmToken() {
        val user = firebaseAuth.currentUser ?: return

        try {
           
            val token = firebaseMessaging.token.await()

            val data = hashMapOf("fcmToken" to token)

            firebaseFirestore.collection("users")
                .document(user.uid)
                .set(data, SetOptions.merge())
                .await()

           
            firebaseMessaging.subscribeToTopic("announcements").await()
            firebaseMessaging.subscribeToTopic("updates").await()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    override fun signOut() {
        firebaseAuth.signOut()
        try { googleSignInClient.signOut() } catch (_: Exception) {}
    }
}