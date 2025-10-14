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

    private val _userRole = MutableLiveData<String>()
    val userRole: LiveData<String> = _userRole

    fun saveFcmToken(token: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .update("fcmToken", token)
        }
    }

    fun checkAuthState() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _authState.value = AuthState.Unauthenticated
            _userRole.value = "user"
        } else {
            if (currentUser.isEmailVerified) {
                fetchUserRole(currentUser.uid)
                fetchUserName(currentUser.uid)
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.NeedsVerification
            }
        }
    }

    private fun fetchUserRole(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                _userRole.value = document.getString("role") ?: "user"
            }
            .addOnFailureListener {
                _userRole.value = "user"
            }
    }

    private fun fetchUserName(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                _currentUserName.value = document.getString("name") ?: "User"
            }
            .addOnFailureListener {
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
                    user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                        if(verificationTask.isSuccessful) {
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
        auth.currentUser?.reload()?.addOnCompleteListener {
            checkAuthState()
        }
    }
    private fun createUserProfile(user: FirebaseUser, name: String) {
        val uid = user.uid
        val newUser = User(
            id = uid,
            name = name,
            avatarUrl = "https://placehold.co/100",
            role = "user"
        )

        db.collection("users").document(uid).set(newUser)
            .addOnSuccessListener {
                _authState.value = AuthState.NeedsVerification
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error("Failed to save user profile: ${e.message}")
            }
    }

    fun enterGuestMode() {
        _authState.value = AuthState.Guest
    }

    fun signout() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .update("fcmToken", null)
        }
        auth.signOut()
        _authState.value = AuthState.Guest
        _userRole.value = "user"
    }
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object NeedsVerification : AuthState()
    object Loading : AuthState()
    object Guest : AuthState()
    data class Error(val message: String) : AuthState()
}
