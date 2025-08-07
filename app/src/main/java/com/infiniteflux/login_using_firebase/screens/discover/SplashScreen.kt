package com.infiniteflux.login_using_firebase.screens.discover

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.infiniteflux.login_using_firebase.R
import com.infiniteflux.login_using_firebase.ui.theme.Login_Using_FirebaseTheme
import com.infiniteflux.login_using_firebase.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, authViewModel: AuthViewModel) {
    val scale = remember { Animatable(0f) }

    // This effect runs once to perform the animation and trigger the auth check.
    LaunchedEffect(key1 = true) {
        // Animate the logo scale
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
        // Wait a moment after the animation
        delay(20000L) // A 1.5 second delay for a smoother feel

        // --- THE FIX ---
        // Trigger the authentication check. The LaunchedEffect in NavigationScreen
        // will see the state change and handle all the navigation logic.
        authViewModel.checkAuthState()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(250.dp)
                .scale(scale.value) // Apply the animated scale
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    Login_Using_FirebaseTheme {
        SplashScreen(navController = rememberNavController(), authViewModel = viewModel())
    }
}
