# 🎓 MSU TJ - University Schedule App

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-blue.svg)
![Go](https://img.shields.io/badge/Backend-Go_1.25-00ADD8?logo=go&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-Auth%20%7C%20Firestore%20%7C%20Cloud%20Functions-orange.svg)

**MSU TJ** is a mobile application designed for students of the Lomonosov Moscow State University (Dushanbe Branch). It helps students track their class schedules, find available classrooms, and receive real-time notifications from the university.

<p align="center">
  <a href="https://github.com/yusufjon-developer/msu-tj-android/releases/tag/v1.0">
    <img src="https://img.shields.io/badge/Download-APK%20v1.0.0-blue?style=for-the-badge&logo=android" alt="Download APK">
  </a>
</p>

## ✨ Key Features

* **📅 Class Schedule:** View daily schedules for different student groups and courses.
* **🏫 Free Classrooms:** Smart filter to find empty auditoriums based on time and day of the week.
* **🔔 Push Notifications:**
    * Instant alerts for schedule changes or university news.
    * Powered by the **Go Backend** + **Firebase Cloud Functions**.
    * Real-time unread message counter (Badge) and notification history.
* **👤 Student Profile:** Manage faculty and course details with cloud synchronization.
* **🔐 Authentication:** Secure sign-in via Email/Password and **Google Sign-In**.

## 📱 Screenshots

|               Authentication               |                   Schedule                    |                   Free Rooms                   |                   Notifications                    |
|:------------------------------------------:|:---------------------------------------------:|:----------------------------------------------:|:--------------------------------------------------:|
| <img src="assets/login.jpg" width="250" /> | <img src="assets/schedule.jpg" width="250" /> | <img src="assets/freerooms.jpg" width="250" /> | <img src="assets/notifications.jpg" width="250" /> |

## 🛠 Tech Stack

### Android App (Client)
* **Language:** Kotlin
* **UI:** Jetpack Compose (Material Design 3)
* **Architecture:** Clean Architecture + MVI (Model-View-Intent)
* **DI (Dependency Injection):** Koin
* **Concurrency:** Kotlin Coroutines & Flow
* **Navigation:** Jetpack Navigation Compose

### Backend Service (Go)
* **Language:** Golang
* **Purpose:** Parsing schedule data, managing student data, and triggering system notifications.
* **Integration:** Firebase Admin SDK for writing to Firestore.

### Cloud Services (Firebase)
* **Authentication:** User management and secure session handling.
* **Cloud Firestore:** Real-time NoSQL database.
* **Cloud Messaging (FCM):** Delivery of push notifications.
* **Cloud Functions (Node.js):** Serverless triggers that listen for database updates and send FCM payloads.

## 🏗 System Architecture

The project follows an **Event-Driven Architecture**:

1.  The **Go Backend** parses data or an admin creates a notification -> Writes to **Firestore**.
2.  A **Cloud Function** (`onDocumentCreated`) detects the new record in the `notifications` collection.
3.  The function retrieves the user's token and sends the payload via **FCM**.
4.  The **Android App** receives the push via `MsuFirebaseMessagingService` and displays the notification.

## 🚀 Getting Started

### 1. Android Client
1.  Clone the repository:
    ```bash
    git clone https://github.com/yusufjon-developer/msu-tj-android.git
    ```
2.  **Important:** This project requires a `google-services.json` file.
    * Create a project in the [Firebase Console](https://console.firebase.google.com/).
    * Download the configuration file and place it in the `app/` directory.
3.  Open the project in Android Studio and wait for Gradle synchronization.
4.  Run on an Emulator or a Physical Device.

### 2. Go Backend
To run the backend, you need a Firebase Service Account key.

1.  Clone the repository:
    ```bash
    git clone https://github.com/yusufjon-developer/msu-tj-backend.git
    ```
2.  Place your `serviceAccountKey.json` file in the root of the project.
3.  Run the server:
    ```bash
    go run cmd/app/main.go
    ```

## 📄 License

This project is distributed under the MIT License. See the [LICENSE](LICENSE) file for more details.