package com.infiniteflux.login_using_firebase.navcontroller

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.infiniteflux.login_using_firebase.AppRoutes
import com.infiniteflux.login_using_firebase.screens.chat.*
import com.infiniteflux.login_using_firebase.sharedComponents.BottomNavigationBar
import com.infiniteflux.login_using_firebase.screens.event.EventDetailsScreen
import com.infiniteflux.login_using_firebase.screens.event.EventsScreen
import com.infiniteflux.login_using_firebase.screens.discover.HomeScreen
import com.infiniteflux.login_using_firebase.screens.discover.SplashScreen
import com.infiniteflux.login_using_firebase.screens.event.CreateEventScreen
import com.infiniteflux.login_using_firebase.screens.event.RateAttendeesScreen
import com.infiniteflux.login_using_firebase.screens.login.LoginScreen
import com.infiniteflux.login_using_firebase.screens.profile.ProfileScreen
import com.infiniteflux.login_using_firebase.screens.login.SignUpScreen
import com.infiniteflux.login_using_firebase.screens.login.VerificationScreen
import com.infiniteflux.login_using_firebase.screens.profile.EditProfileScreen
import com.infiniteflux.login_using_firebase.screens.profile.WallOfShame.WallOfShameScreen
import com.infiniteflux.login_using_firebase.screens.profile.connection.ConnectionScreen
import com.infiniteflux.login_using_firebase.viewmodel.*

@Composable
fun NavigationScreen(modifier: Modifier = Modifier, authViewModel: AuthViewModel,
                     chatViewModel: ChatViewModel, eventsViewModel: EventsViewModel,
                     profileViewModel: ProfileViewModel,
                     homeViewModel: HomeViewModel,
                     connectionViewModel: ConnectionViewModel,
                     wallOfShameViewModel: WallOfShameViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val authState by authViewModel.authState.observeAsState()
    var showLoginPrompt by remember { mutableStateOf(false) }
    val onLoginRequired = { showLoginPrompt = true }

    val screensWithBottomBar = listOf(
        AppRoutes.HOME,
        AppRoutes.EVENTS,
        AppRoutes.CHATS,
        AppRoutes.PROFILE
    )

    // This LaunchedEffect is the single source of truth for auth navigation
    // after the initial splash screen.
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                // When a user logs in, re-fetch all their data
                homeViewModel.initializeData()
                chatViewModel.initializeData()
                eventsViewModel.initializeData()
                profileViewModel.initializeData()
                connectionViewModel.initializeData()

                navController.navigate(AppRoutes.HOME) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            is AuthState.Unauthenticated -> {
                // After logout, enter guest mode, which will then trigger navigation to Events
                authViewModel.enterGuestMode()
            }
            is AuthState.Guest -> {
                // When entering guest mode, navigate to the Events screen
                navController.navigate(AppRoutes.EVENTS) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            is AuthState.NeedsVerification -> {
                navController.navigate(AppRoutes.VERIFICATION) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            else -> Unit // Do nothing for Loading or initial null state
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            // Show bottom bar for guests ONLY on the Events screen
            if (currentRoute == AppRoutes.EVENTS || (currentRoute in screensWithBottomBar && authState is AuthState.Authenticated)) {
                BottomNavigationBar(
                    navController = navController,
                    authState = authState,
                    onLoginRequired = onLoginRequired
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                // --- THE FIX: Start the app on the SplashScreen ---
                startDestination = AppRoutes.SPLASH
            ) {
                composable(AppRoutes.SPLASH) {
                    SplashScreen(navController = navController, authViewModel = authViewModel)
                }
                composable(AppRoutes.LOGIN) {
                    LoginScreen(navController, authViewModel)
                }
                composable(AppRoutes.SIGNUP) {
                    SignUpScreen(navController, authViewModel)
                }
                composable(AppRoutes.HOME) {
                    HomeScreen(navController, viewModel = homeViewModel,authViewModel=authViewModel)
                }
                composable(AppRoutes.EVENTS) {
                    EventsScreen(
                        navController = navController,
                        viewModel = eventsViewModel,
                        authState = authState,
                        onLoginRequired = onLoginRequired
                    )
                }
                composable(
                    route = "${AppRoutes.EVENT_DETAILS}/{eventId}",
                    arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString("eventId")
                    if (eventId != null) {
                        EventDetailsScreen(
                            navController = navController,
                            eventId = eventId,
                            viewModel = eventsViewModel,
                            authState = authState,
                            onLoginRequired = onLoginRequired
                        )
                    }
                }
                composable(AppRoutes.CHATS) {
                    ChatScreen(navController = navController, viewModel = chatViewModel, authViewModel = authViewModel)
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
                            viewModel = chatViewModel,
                            authViewModel = authViewModel
                        )
                    }
                }
                composable(
                    route = "${AppRoutes.GROUP_INFO}/{groupId}",
                    arguments = listOf(navArgument("groupId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val groupId = backStackEntry.arguments?.getString("groupId")
                    if (groupId != null) {
                        GroupInfoScreen(navController = navController, groupId = groupId, viewModel = chatViewModel,authViewModel=authViewModel)
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
                        viewModel = profileViewModel,
                        authViewModel = authViewModel,
                        chatViewModel = chatViewModel,
                        eventsViewModel = eventsViewModel,
                        homeViewModel = homeViewModel,
                        connectionViewModel=connectionViewModel
                    )
                }
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
                composable(AppRoutes.CREATE_EVENT) {
                    CreateEventScreen(navController = navController, viewModel = eventsViewModel)
                }
                composable(AppRoutes.EDITPROFILE) {
                    EditProfileScreen(navController = navController, viewModel = profileViewModel)
                }

                composable(AppRoutes.CONNECTION){
                    ConnectionScreen(navController = navController, viewModel = connectionViewModel)
                }

                composable(AppRoutes.WALLOFSHAME){
                    WallOfShameScreen(navController = navController, viewModel= wallOfShameViewModel)
                }

                composable(
                    route = "${AppRoutes.RATE_ATTENDEES}/{eventId}",
                    arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString("eventId")
                    if (eventId != null) {
                        RateAttendeesScreen(navController = navController, eventId = eventId)
                    }
                }
            }

            if (showLoginPrompt) {
                LoginPromptDialog(
                    onDismiss = { showLoginPrompt = false },
                    onLoginClick = {
                        showLoginPrompt = false
                        navController.navigate(AppRoutes.LOGIN)
                    }
                )
            }
        }
    }
}

@Composable
fun LoginPromptDialog(onDismiss: () -> Unit, onLoginClick: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Login Required") },
        text = { Text("You need to be logged in to use this feature. Please log in or create an account to continue.") },
        confirmButton = {
            Button(onClick = onLoginClick) {
                Text("Login")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
