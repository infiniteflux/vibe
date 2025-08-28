package com.infiniteflux.login_using_firebase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold

import androidx.lifecycle.viewmodel.compose.viewModel
import com.infiniteflux.login_using_firebase.navcontroller.NavigationScreen
import com.infiniteflux.login_using_firebase.ui.theme.Login_Using_FirebaseTheme
import com.infiniteflux.login_using_firebase.viewmodel.ChatViewModel
import com.infiniteflux.login_using_firebase.viewmodel.AuthViewModel
import com.infiniteflux.login_using_firebase.viewmodel.ConnectionViewModel
import com.infiniteflux.login_using_firebase.viewmodel.EventsViewModel
import com.infiniteflux.login_using_firebase.viewmodel.HomeViewModel
import com.infiniteflux.login_using_firebase.viewmodel.ProfileViewModel
import com.infiniteflux.login_using_firebase.viewmodel.ReportViewModel
import com.infiniteflux.login_using_firebase.viewmodel.WallOfShameViewModel

object AppRoutes {
    const val CHAT_GROUP_DETAILS = "chat_group_details"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
    const val EVENTS = "events"
    const val EVENT_DETAILS = "event_details" // Base route for details
    const val CHATS = "chats"
    const val PROFILE = "profile"
    const val GROUP_INFO = "group_info"
    const val ADD_MEMBER_TO_GROUP = "add_member_to_group"
    const val VERIFICATION ="Verification"
    const val CREATE_EVENT = "Create Event"
    const val EDITPROFILE ="Edit profile"
    const val SPLASH = "SplashScreen"
    const val CONNECTION = "Connection"
    const val WALLOFSHAME = "Wall Of Shame"
    const val RATE_ATTENDEES= "Rate_Attendees"
    const val PRIVATE_CHAT = "private_chat"
    const val REPORT_USER = "REPORT_USER"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Login_Using_FirebaseTheme {
                val authViewModel: AuthViewModel = viewModel()
                val eventsViewModel: EventsViewModel = viewModel()
                val chatViewModel: ChatViewModel = viewModel()
                val profileViewModel: ProfileViewModel = viewModel()
                val homeViewModel: HomeViewModel = viewModel ()
                val connectionViewModel: ConnectionViewModel = viewModel()
                val WallOfShameViewModel: WallOfShameViewModel = viewModel()
                val ReportViewModel: ReportViewModel = viewModel ()

                NavigationScreen(
                    authViewModel = authViewModel,
                    eventsViewModel = eventsViewModel,
                    chatViewModel = chatViewModel,
                    profileViewModel = profileViewModel,
                    homeViewModel = homeViewModel,
                    connectionViewModel = connectionViewModel,
                    wallOfShameViewModel = WallOfShameViewModel,
                    reportViewModel = ReportViewModel
                )
            }
        }
    }
}



