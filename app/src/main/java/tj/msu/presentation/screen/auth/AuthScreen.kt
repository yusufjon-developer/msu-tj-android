package tj.msu.presentation.screen.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import tj.msu.R
import tj.msu.presentation.theme.MsuBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val googleSignInClient: GoogleSignInClient = koinInject()

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { token ->
                    viewModel.setEvent(AuthEvent.OnGoogleSignIn(token))
                }
            } catch (_: ApiException) {
                Toast.makeText(context, "Google Sign In Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AuthEffect.NavigateToMain -> onAuthSuccess()
                is AuthEffect.ShowError -> Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    BackHandler(enabled = state.currentStep == AuthStep.PROFILE_INFO) {
        viewModel.setEvent(AuthEvent.OnBackStep)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = state.currentStep,
            transitionSpec = {
                if (targetState == AuthStep.PROFILE_INFO) {
                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                } else {
                    (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(slideOutHorizontally { width -> width } + fadeOut())
                }
            },
            label = "auth_step"
        ) { step ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {

                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Logo",
                    contentScale = ContentScale.Inside,
                    modifier = Modifier
                        .size(144.dp)

                )

                Text(
                    text = when {
                        step == AuthStep.PROFILE_INFO -> "Информация о вас"
                        state.isLoginMode -> "Вход"
                        else -> "Регистрация"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MsuBlue
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (step == AuthStep.CREDENTIALS) {

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { viewModel.setEvent(AuthEvent.OnEmailChange(it)) },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = state.error != null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.pass,
                        onValueChange = { viewModel.setEvent(AuthEvent.OnPassChange(it)) },
                        label = { Text("Пароль") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = state.error != null
                    )

                    if (state.error != null) {
                        Text(
                            text = state.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.setEvent(AuthEvent.OnNextStep) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MsuBlue),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(if (state.isLoginMode) "Войти" else "Далее", fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { googleLauncher.launch(googleSignInClient.signInIntent) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Google",
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    TextButton(onClick = { viewModel.setEvent(AuthEvent.OnToggleMode) }) {
                        Text(
                            text = if (state.isLoginMode) "Нет аккаунта? Зарегистрироваться" else "Уже есть аккаунт? Войти",
                            color = MsuBlue
                        )
                    }

                } else {
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { viewModel.setEvent(AuthEvent.OnNameChange(it)) },
                        label = { Text("Имя и Фамилия") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    var facultyExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = facultyExpanded,
                        onExpandedChange = { facultyExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = state.faculties[state.selectedFacultyCode] ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Направление") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = facultyExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = facultyExpanded,
                            onDismissRequest = { facultyExpanded = false }
                        ) {
                            state.faculties.forEach { (code, title) ->
                                DropdownMenuItem(
                                    text = { Text(title) },
                                    onClick = {
                                        viewModel.setEvent(AuthEvent.OnFacultyChange(code))
                                        facultyExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    var courseExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = courseExpanded,
                        onExpandedChange = { courseExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = "${state.selectedCourse} курс",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Курс") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = courseExpanded,
                            onDismissRequest = { courseExpanded = false }
                        ) {
                            state.courses.forEach { course ->
                                DropdownMenuItem(
                                    text = { Text("$course курс") },
                                    onClick = {
                                        viewModel.setEvent(AuthEvent.OnCourseChange(course))
                                        courseExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (state.error != null) {
                        Text(
                            text = state.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedButton(
                            onClick = { viewModel.setEvent(AuthEvent.OnBackStep) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = { viewModel.setEvent(AuthEvent.OnNextStep) },
                            modifier = Modifier
                                .weight(2f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MsuBlue),
                            enabled = !state.isLoading
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Завершить", fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}