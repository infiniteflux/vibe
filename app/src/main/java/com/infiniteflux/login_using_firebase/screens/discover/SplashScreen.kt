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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.infiniteflux.login_using_firebase.AppRoutes
import com.infiniteflux.login_using_firebase.ui.theme.Login_Using_FirebaseTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val scale = remember { Animatable(0f) }

    // This effect runs once when the composable enters the screen
    LaunchedEffect(key1 = true) {
        // Animate the scale of the logo from 0f to 1f
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
        // Wait for 1.2 seconds
        delay(1200L)
        // Navigate to the login screen and clear the back stack
        navController.navigate(AppRoutes.LOGIN) {
            popUpTo(AppRoutes.SPLASH) { inclusive = true }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Match the logo's background
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
        SplashScreen(navController = rememberNavController())
    }
}
