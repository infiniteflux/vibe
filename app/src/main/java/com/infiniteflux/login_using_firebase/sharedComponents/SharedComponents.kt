package com.infiniteflux.login_using_firebase.sharedComponents

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.infiniteflux.login_using_firebase.AppRoutes
import com.infiniteflux.login_using_firebase.viewmodel.AuthState

@Composable
fun BottomNavigationBar(
    navController: NavController,
    authState: AuthState?,
    onLoginRequired: () -> Unit
) {
    val items = listOf(
        "Discover" to AppRoutes.HOME,
        "Events" to AppRoutes.EVENTS,
        "Chats" to AppRoutes.CHATS,
        "Profile" to AppRoutes.PROFILE
    )
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Event,
        Icons.Default.ChatBubble,
        Icons.Default.Person
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEachIndexed { index, (screen, route) ->
            NavigationBarItem(
                icon = { Icon(icons[index], contentDescription = screen) },
                label = { Text(screen) },
                selected = currentDestination?.hierarchy?.any { it.route == route } == true,
                onClick = {
                    val protectedRoutes = listOf(AppRoutes.HOME, AppRoutes.CHATS, AppRoutes.PROFILE)

                    if (route in protectedRoutes && authState is AuthState.Guest) {
                        onLoginRequired()
                    } else {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
