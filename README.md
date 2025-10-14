Vibe - Social Event & Connection App
Vibe is a modern, real-time social application built with Kotlin and Jetpack Compose. It allows users to discover and join local events, connect with other attendees through a unique post-event rating system, and engage in both group and private one-on-one chats.
The app is powered by a robust Firebase backend, providing real-time data synchronization, authentication, push notifications, and scalable storage for user-generated content.

---
## Download

You can download the latest version of the Vibe app directly from our repository:

* **[Download Vibe APK (Latest Version)](./apk/vibe.apk)**

---

Architecture Diagram
The application follows a modern client-server architecture using Firebase as the backend-as-a-service (BaaS) platform. The diagram below illustrates the flow of data and interactions between the client, backend services, and automated functions.

![Application Architecture Diagram](./app/img/diagram.png)

The diagram shows the Android app's UI and ViewModels interacting with Firebase services. Firebase triggers Cloud Functions for backend logic, which in turn use services like FCM to send push notifications back to the app.

---
## Key Features

### Dynamic Guest & Authenticated Mode
Users can browse events without an account. Protected actions (joining events, chatting) trigger a login prompt.

### Secure User Authentication
Full sign-up, login, and email verification flow.

### Real-time Event System
* Creators can dynamically create events with image uploads.
* Users can view and join upcoming events.
* A "Trending Events" section on the home screen automatically shows the most popular upcoming events.

### Post-Event Connection System
* After an event ends, attendees can rate each other.
* Mutual "Spark" ratings automatically create a connection between users.
* A dedicated "My Connections" screen lists all successful matches.

### Real-time Chat
* **Group Chat:** Users can create and participate in group conversations.
* **Private 1-on-1 Chat:** Connected users can start private chats.
* **Live Updates:** Features unread message indicators and last message previews, all updated in real-time.

### Role-Based Access Control
* Users can be assigned "user" or "creator" roles.
* Features like creating events and adding members to groups are restricted to creators.

### Community Moderation
* A full user reporting system.
* A "Wall of Shame" to display users who have been banned by an admin.

### Push Notifications
* Real-time alerts for new messages, new connections, and new events.
* A fully functional in-app notification center to view past notifications.

### Automated Backend Logic
Cloud Functions automatically manage event join counts, data cleanup when events are deleted, and the sending of all push notifications.

---

## Technology Stack

### Frontend
* **Kotlin:** The primary programming language.
* **Jetpack Compose:** For building the entire declarative UI.
* **Compose Navigation:** For handling all in-app navigation and state management.
* **Coil:** For asynchronous image loading.

### Backend (Firebase)
* **Firebase Authentication:** For user management and security.
* **Firestore Database:** As the real-time, NoSQL database for all app data.
* **Firebase Cloud Storage:** For storing user-uploaded images.
* **Cloud Functions for Firebase:** For all server-side logic and automation.
* **Firebase Cloud Messaging (FCM):** For handling push notifications.

---

## Setup & Configuration

To run this project, you will need to:

### 1. Set up a Firebase Project
* Create a new project in the [Firebase Console](https://console.firebase.google.com/).
* Add an Android app to the project with the package name `com.infiniteflux.login_using_firebase`.
* Download the `google-services.json` file and place it in the `app/` directory of the Android project.
* Add your debug and release SHA-1 keys to the project settings in Firebase.

### 2. Enable Firebase Services
* In the Firebase Console, enable **Authentication** (with the Email/Password provider).
* Enable **Firestore Database**.
* Enable **Firebase Storage**.

### 3. Deploy Cloud Functions
* Install [Node.js](https://nodejs.org/en/download/) and the [Firebase CLI](https://firebase.google.com/docs/cli).
* Navigate to the `functions` directory in your terminal (assuming your functions are in a `functions` folder within your project).
* Run `npm install` to install dependencies.
* Run `firebase deploy --only functions` to deploy the backend logic.

### 4. Create Firestore Indexes
* Run the app and navigate to the `HomeScreen` and `RateAttendeesScreen`.
* Check the Android Studio Logcat for error messages containing links to create the required Firestore indexes.
* Click these links and save the indexes in the Firebase Console.


