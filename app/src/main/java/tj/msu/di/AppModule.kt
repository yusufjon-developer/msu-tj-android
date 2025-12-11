package tj.msu.di

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import tj.msu.R

@Module
@ComponentScan("tj.msu")
class AppModule {

    @Single
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Single
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Single
    fun provideGoogleSignInOptions(context: Context): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    @Single
    fun provideGoogleSignInClient(context: Context, options: GoogleSignInOptions): GoogleSignInClient {
        return GoogleSignIn.getClient(context, options)
    }
}