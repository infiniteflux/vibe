package com.infiniteflux.login_using_firebase.navcontroller

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.infiniteflux.login_using_firebase.AppRoutes
import com.infiniteflux.login_using_firebase.screens.chat.AddMemberScreen
import com.infiniteflux.login_using_firebase.screens.chat.ChatScreen
import com.infiniteflux.login_using_firebase.sharedComponents.BottomNavigationBar
import com.infiniteflux.login_using_firebase.screens.event.EventDetailsScreen
import com.infiniteflux.login_using_firebase.screens.event.EventsScreen
import com.infiniteflux.login_using_firebase.screens.chat.GroupChatScreen
import com.infiniteflux.login_using_firebase.screens.chat.GroupInfoScreen
import com.infiniteflux.login_using_firebase.screens.discover.HomeScreen
import com.infiniteflux.login_using_firebase.screens.event.CreateEventScreen
import com.infiniteflux.login_using_firebase.screens.login.LoginScreen
import com.infiniteflux.login_using_firebase.screens.profile.ProfileScreen
import com.infiniteflux.login_using_firebase.screens.login.SignUpScreen
import com.infiniteflux.login_using_firebase.screens.login.VerificationScreen
import com.infiniteflux.login_using_firebase.screens.profile.EditProfileScreen
import com.infiniteflux.login_using_firebase.viewmodel.AuthState
import com.infiniteflux.login_using_firebase.viewmodel.ChatViewModel
import com.infiniteflux.login_using_firebase.viewmodel.AuthViewModel
import com.infiniteflux.login_using_firebase.viewmodel.EventsViewModel
import com.infiniteflux.login_using_firebase.viewmodel.HomeViewModel
import com.infiniteflux.login_using_firebase.viewmodel.ProfileViewModel

@Composable
fun NavigationScreen(modifier: Modifier = Modifier, authViewModel: AuthViewModel,
                     chatViewModel: ChatViewModel, eventsViewModel: EventsViewModel,
                     profileViewModel: ProfileViewModel,
                     homeViewModel: HomeViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val authState by authViewModel.authState.observeAsState()

    val screensWithBottomBar = listOf(
        AppRoutes.HOME,
        AppRoutes.EVENTS,
        AppRoutes.CHATS,
        AppRoutes.PROFILE
    )

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (currentRoute in screensWithBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        // --- THE FIX: The navigation logic is now here, outside the NavHost ---
        // This LaunchedEffect will always be active and can react to auth changes
        // from any screen in your app.
        LaunchedEffect(authState) {
            when (authState) {
                is AuthState.Authenticated -> {
                    navController.navigate(AppRoutes.HOME) {
                        // Clear the entire back stack
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
                is AuthState.NeedsVerification -> {
                    navController.navigate(AppRoutes.VERIFICATION) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
                is AuthState.Unauthenticated -> {
                    // Only navigate to login if we are not already on an auth screen
                    if (currentRoute != AppRoutes.LOGIN && currentRoute != AppRoutes.SIGNUP) {
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                }
                else -> Unit // Do nothing for Loading or initial null state
            }
        }

        NavHost(
            navController = navController,
            // The start destination can now be your login screen. The LaunchedEffect will handle redirection.
            startDestination = AppRoutes.LOGIN,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Your existing routes
            composable(AppRoutes.LOGIN) {
                LoginScreen(navController, authViewModel)
            }
            composable(AppRoutes.SIGNUP) {
                SignUpScreen(navController, authViewModel)
            }
            composable(AppRoutes.HOME) {
                HomeScreen(navController,  viewModel = homeViewModel)
            }
            composable(AppRoutes.EVENTS) {
                EventsScreen(navController = navController, viewModel = eventsViewModel)
            }

            composable(
                route = "${AppRoutes.EVENT_DETAILS}/{eventId}",
                // 1. Change the argument type to StringType
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                // 2. Get the argument as a String
                val eventId = backStackEntry.arguments?.getString("eventId")
                if (eventId != null) {
                    EventDetailsScreen(
                        navController = navController,
                        eventId = eventId,
                        viewModel = eventsViewModel
                    )
                }
            }

            composable(AppRoutes.CHATS) {
                ChatScreen(navController = navController, viewModel = chatViewModel)
            }
            composable(
                route = "${AppRoutes.CHAT_GROUP_DETAILS}/{groupId}/{groupName}",
                arguments = listOf(
                    navArgument("groupId") { type = NavType.StringType },
                    navArgument("groupName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId")
                val groupName = backStackEntry.arguments?.getString("groupName")
                if (groupId != null && groupName != null) {
                    GroupChatScreen(
                        navController = navController,
                        groupId = groupId,
                        groupName = groupName,
                        viewModel = chatViewModel
                    )
                }
            }
            composable(
                route = "${AppRoutes.GROUP_INFO}/{groupId}",
                arguments = listOf(navArgument("groupId") { type = NavType.StringType })
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId")
                if (groupId != null) {
                    GroupInfoScreen(navController = navController, groupId = groupId, viewModel = chatViewModel)
                }
            }
            composable(
                route = "${AppRoutes.ADD_MEMBER_TO_GROUP}/{groupId}",
                arguments = listOf(navArgument("groupId") { type = NavType.StringType })
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId")
                if (groupId != null) {
                    AddMemberScreen(navController = navController, groupId = groupId, viewModel = chatViewModel)
                }
            }
            composable(AppRoutes.PROFILE) {
                ProfileScreen(
                    navController = navController,
                    viewModel = profileViewModel, // Pass profileViewModel to 'viewModel'
                    authViewModel = authViewModel   // Pass authViewModel to 'authViewModel'
                )
            }
            // Add the new route for the Verification Screen
            composable(AppRoutes.VERIFICATION) {
                VerificationScreen(
                    onCheckVerification = {
                        authViewModel.reloadUserAndCheckVerification()
                    },
                    onSignOut = {
                        authViewModel.signout()
                    }
                )
            }

            composable (AppRoutes.CREATE_EVENT){
                CreateEventScreen(navController= navController, viewModel = eventsViewModel)
            }

            composable ( AppRoutes.EDITPROFILE ){
                EditProfileScreen(navController = navController, viewModel = profileViewModel)
            }
        }
    }
}
