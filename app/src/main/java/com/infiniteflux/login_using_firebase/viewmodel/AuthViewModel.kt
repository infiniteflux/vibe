package com.infiniteflux.login_using_firebase.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.infiniteflux.login_using_firebase.data.User


class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _currentUserName = MutableLiveData<String>()
    val currentUserName: LiveData<String> = _currentUserName

    init {
        checkAuthState()
    }

    fun checkAuthState() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            // --- CHANGE 1: Check if the user's email is verified ---
            if (currentUser.isEmailVerified) {
                fetchUserName(currentUser.uid)
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.NeedsVerification
            }
        }
    }

    // --- 3. Add the new function to fetch the user's name from Firestore ---
    private fun fetchUserName(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                _currentUserName.value = document.getString("name") ?: "User"
            }
            .addOnFailureListener {
                // Handle the error, maybe set a default name
                _currentUserName.value = "User"
            }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // --- CHANGE 2: After login, check for verification again ---
                    checkAuthState()
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something Went Wrong")
                }
            }
    }

    fun signup(name: String, email: String, password: String) {
        if (name.isBlank() || email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Name, email and password cannot be empty")
            return
        }
        _authState.value = AuthState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // --- CHANGE 3: Send the verification email ---
                    user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                        if(verificationTask.isSuccessful) {
                            // --- CHANGE 4: Create the user profile, but move to NeedsVerification state ---
                            createUserProfile(user, name)
                        } else {
                            _authState.value = AuthState.Error("Failed to send verification email.")
                        }
                    }
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something Went Wrong")
                }
            }
    }

    fun reloadUserAndCheckVerification() {
        _authState.value = AuthState.Loading
        // First, reload the user data from Firebase
        auth.currentUser?.reload()?.addOnCompleteListener {
            // After reloading, check the auth state again
            checkAuthState()
        }
    }

    // --- CHANGE 5: Helper function to create the user profile ---
    private fun createUserProfile(user: FirebaseUser, name: String) {
        val uid = user.uid
        val newUser = User(
            id = uid,
            name = name,
            avatarUrl = "https://placehold.co/100" // Default avatar
        )

        db.collection("users").document(uid).set(newUser)
            .addOnSuccessListener {
                // Profile saved, now tell the UI to show the "verify email" message
                _authState.value = AuthState.NeedsVerification
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error("Failed to save user profile: ${e.message}")
            }
    }


    fun signout(){
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    // --- CHANGE 6: Add a new state for users who need to verify their email ---
    object NeedsVerification : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}
