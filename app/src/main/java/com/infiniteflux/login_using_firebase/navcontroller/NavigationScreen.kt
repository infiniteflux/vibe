package com.infiniteflux.login_using_firebase.navcontroller

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.infiniteflux.login_using_firebase.screens.login.LoginScreen
import com.infiniteflux.login_using_firebase.screens.profile.ProfileScreen
import com.infiniteflux.login_using_firebase.screens.login.SignUpScreen
import com.infiniteflux.login_using_firebase.viewmodel.ChatViewModel
import com.infiniteflux.login_using_firebase.viewmodel.AuthViewModel
import com.infiniteflux.login_using_firebase.viewmodel.EventsViewModel
import com.infiniteflux.login_using_firebase.viewmodel.ProfileViewModel

@Composable
fun NavigationScreen(modifier: Modifier = Modifier, authViewModel: AuthViewModel,
                     chatViewModel: ChatViewModel, eventsViewModel: EventsViewModel,
                     profileViewModel: ProfileViewModel
) {
    val navController = rememberNavController()

    // This state will help us determine the current screen
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // List of screens that should have the bottom navigation bar
    val screensWithBottomBar = listOf(
        AppRoutes.HOME,
        AppRoutes.EVENTS,
        AppRoutes.CHATS,
        AppRoutes.PROFILE
    )

    Scaffold(
        modifier = modifier,
        bottomBar = {
            // Only show the bottom bar if the current route is in our list
            if (currentRoute in screensWithBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.LOGIN,
            modifier = Modifier.padding(innerPadding) // So NavHost respects Scaffold's padding
        ) {
            composable(AppRoutes.LOGIN) {
                LoginScreen(navController, authViewModel)
            }
            composable(AppRoutes.SIGNUP) {
                SignUpScreen(navController, authViewModel)
            }
            composable(AppRoutes.HOME) {
                HomeScreen(navController, authViewModel)
            }
            composable(AppRoutes.EVENTS) {
                EventsScreen(navController = navController, viewModel = eventsViewModel)
            }
            composable(
                route = "${AppRoutes.EVENT_DETAILS}/{eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.IntType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getInt("eventId")
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
                // 1. Add groupName to the route
                route = "${AppRoutes.CHAT_GROUP_DETAILS}/{groupId}/{groupName}",
                arguments = listOf(
                    // 2. Change groupId type to String
                    navArgument("groupId") { type = NavType.StringType },
                    // 3. Add an argument for groupName
                    navArgument("groupName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                // 4. Get the arguments as Strings
                val groupId = backStackEntry.arguments?.getString("groupId")
                val groupName = backStackEntry.arguments?.getString("groupName")

                if (groupId != null && groupName != null) {
                    GroupChatScreen(
                        navController = navController,
                        groupId = groupId,
                        groupName = groupName, // 5. Pass the groupName to the screen
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
                ProfileScreen(navController =navController, viewModel = profileViewModel)
            }

        }
    }
}