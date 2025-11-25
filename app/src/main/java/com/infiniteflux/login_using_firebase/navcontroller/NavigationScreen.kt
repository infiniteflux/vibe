package com.infiniteflux.login_using_firebase.navcontroller

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.infiniteflux.login_using_firebase.AppRoutes
import com.infiniteflux.login_using_firebase.screens.Notification.NotificationScreen
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
import com.infiniteflux.login_using_firebase.screens.profile.Report.ReportUserScreen
import com.infiniteflux.login_using_firebase.screens.profile.WallOfShame.WallOfShameScreen
import com.infiniteflux.login_using_firebase.screens.profile.connection.ConnectionScreen
import com.infiniteflux.login_using_firebase.viewmodel.*

@Composable
fun NavigationScreen(modifier: Modifier = Modifier, authViewModel: AuthViewModel,
                     chatViewModel: ChatViewModel, eventsViewModel: EventsViewModel,
                     profileViewModel: ProfileViewModel,
                     homeViewModel: HomeViewModel,
                     connectionViewModel: ConnectionViewModel,
                     wallOfShameViewModel: WallOfShameViewModel,
                     reportViewModel: ReportViewModel,
                     notificationViewModel: NotificationViewModel
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


    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                Firebase.messaging.token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        authViewModel.saveFcmToken(token)
                    }
                }
            }
        }
    )

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                homeViewModel.initializeData()
                chatViewModel.initializeData()
                eventsViewModel.initializeData()
                profileViewModel.initializeData()
                connectionViewModel.initializeData()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    Firebase.messaging.token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val token = task.result
                            authViewModel.saveFcmToken(token)
                        }
                    }
                }

                navController.navigate(AppRoutes.HOME) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }

            is AuthState.Unauthenticated -> {
                authViewModel.enterGuestMode()
            }
            is AuthState.Guest -> {
                navController.navigate(AppRoutes.EVENTS) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            is AuthState.NeedsVerification -> {
                navController.navigate(AppRoutes.VERIFICATION) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            else -> Unit
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
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
                    HomeScreen(navController, viewModel = homeViewModel,authViewModel=authViewModel,
                        connectionViewModel = connectionViewModel)
                }
                composable(AppRoutes.EVENTS) {
                    EventsScreen(
                        navController = navController,
                        viewModel = eventsViewModel,
                        authState = authState,
                        onLoginRequired = onLoginRequired,
                        authViewModel = authViewModel
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
                    ConnectionScreen(navController = navController, viewModel = connectionViewModel, chatViewModel = chatViewModel)
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

                composable(
                    route = "${AppRoutes.PRIVATE_CHAT}/{chatRoomId}/{otherUserName}",
                    arguments = listOf(
                        navArgument("chatRoomId") { type = NavType.StringType },
                        navArgument("otherUserName") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val chatRoomId = backStackEntry.arguments?.getString("chatRoomId")
                    val otherUserName = backStackEntry.arguments?.getString("otherUserName")
                    if (chatRoomId != null && otherUserName != null) {
                        PrivateChatScreen(
                            navController = navController,
                            chatRoomId = chatRoomId,
                            otherUserName = otherUserName,
                            viewModel = chatViewModel,
                            authViewModel=authViewModel
                        )
                    }
                }

                composable(AppRoutes.REPORT_USER) {
                    ReportUserScreen(navController = navController, viewModel = reportViewModel )
                }

                composable (AppRoutes.NOTIFICATION){
                    NotificationScreen(navController = navController, viewModel  = notificationViewModel)
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
