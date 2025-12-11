package tj.msu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import tj.msu.presentation.screen.main.MainScreen
import tj.msu.presentation.theme.MsuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MsuTheme {
                MainScreen()
            }
        }
    }
}