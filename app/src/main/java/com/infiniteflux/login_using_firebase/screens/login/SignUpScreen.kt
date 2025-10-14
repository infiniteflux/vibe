package com.infiniteflux.login_using_firebase.screens.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.infiniteflux.login_using_firebase.AppRoutes
import com.infiniteflux.login_using_firebase.ui.theme.Login_Using_FirebaseTheme
import com.infiniteflux.login_using_firebase.viewmodel.AuthViewModel
import com.infiniteflux.login_using_firebase.viewmodel.AuthState

@Composable
fun SignUpScreen(navController: NavController, authViewModel: AuthViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by authViewModel.authState.observeAsState()
    val context = LocalContext.current

    // --- 2. Update LaunchedEffect to handle the new state ---
//    LaunchedEffect(authState) {
//        when (val state = authState) {
//            is AuthState.Authenticated -> {
//                // This will now only happen if the user is already verified
//                navController.navigate(navController.navigate(AppRoutes.HOME)) {
//                    popUpTo("login") { inclusive = true }
//                }
//            }
//            is AuthState.NeedsVerification -> {
//                // Navigate to the verification screen after successful signup
//                navController.navigate(navController.navigate(AppRoutes.VERIFICATION)) {
//                    popUpTo("login") { inclusive = true }
//                }
//            }
//            is AuthState.Error -> {
//                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
//            }
//            else -> Unit // Handle Loading and Unauthenticated states if needed
//        }
//    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(13.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Create Account",
                fontSize = 24.sp,
                style = TextStyle(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .align(Alignment.Start)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(text = "Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(text = "Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(text = "Password") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    authViewModel.signup(name, email, password)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Sign Up", textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = {
                navController.navigate("login")
            }) {
                Text(text = "Already have an account? Login")
            }
        }
    }
}

@Preview
@Composable
private fun SignupPrev() {
    Login_Using_FirebaseTheme {
        SignUpScreen(navController = rememberNavController(), authViewModel = viewModel())
    }
}