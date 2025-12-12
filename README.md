# 🎓 MSU TJ - University Schedule App

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-purple.svg?logo=kotlin)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-blue.svg?logo=android)
![Firebase](https://img.shields.io/badge/Firebase-Realtime%20DB%20%7C%20FCM-orange.svg?logo=firebase)
![Go](https://img.shields.io/badge/Backend-Go-00ADD8?logo=go&logoColor=white)

**MSU TJ** is a comprehensive mobile application designed for students and faculty of the Lomonosov Moscow State University (Dushanbe Branch). It provides real-time access to class schedules, university news, and classroom availability.

<p align="center">
  <a href="https://github.com/yusufjon-developer/msu-tj-android/releases/tag/v1.1.1">
    <img src="https://img.shields.io/badge/Download-APK%20v1.1.1-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Download APK">
  </a>
</p>

## ✨ Key Features

* **📅 Student Schedule:**
    * View daily schedules filtered by faculty and course.
    * **Swipe Navigation:** Easily switch between days of the week using swipe gestures.
    * **Real-time Sync:** Schedule changes update instantly.
* **👨‍🏫 Teacher Schedule (New!):**
    * Search for instructors by name.
    * View detailed weekly workloads for teachers.
* **🏫 Free Classrooms:** Smart filter to find empty auditoriums based on current time and day.
* **🔔 Push Notifications:**
    * Instant alerts for schedule changes or university news.
    * Powered by **Go Backend** + **Firebase Cloud Functions**.
* **👤 Student Profile:** Manage faculty and course details with cloud synchronization.

## 📱 Screenshots

| Student Schedule | Teacher Search | Teacher Schedule |
|:-------------------------:|:-------------------------:|:-------------------------:|
| <img src="image_bd9211.png" width="250" /> | <img src="image_bd2996.png" width="250" /> | <img src="image_bd8ed2.png" width="250" /> |

## 🛠 Tech Stack

### Android App (Client)
* **Language:** Kotlin
* **UI:** Jetpack Compose (Material Design 3)
* **Architecture:** Clean Architecture + MVI (Model-View-Intent)
* **DI:** Koin
* **Async:** Kotlin Coroutines & Flow
* **Data Sources:**
    * **Firebase Realtime Database:** For live schedule synchronization.
    * **Cloud Firestore:** For user profiles and notifications.

### Backend Service (Go)
* **Language:** Golang
* **Purpose:** Parsing schedule data, managing student records, and triggering system-wide notifications.
* **Integration:** Firebase Admin SDK.

### Cloud Services (Firebase)
* **Authentication:** User management and secure session handling.
* **Realtime Database:** Stores the schedule structure (optimized for JSON trees).
* **Cloud Messaging (FCM):** Delivery of targeted push notifications.
* **Cloud Functions:** Serverless triggers for database events.

## 🏗 System Architecture

The project follows an **Event-Driven Architecture**:

1.  The **Go Backend** parses data or an admin creates a notification -> Writes to **Firestore/Realtime DB**.
2.  The **Android App** observes these data sources via `callbackFlow`.
3.  When data changes, the UI updates automatically (Reactive UI).
4.  For important alerts, a **Cloud Function** triggers an **FCM** payload to subscribed devices/topics.

## 🚀 Getting Started

### 1. Android Client
1.  Clone the repository:
    ```bash
    git clone [https://github.com/yusufjon-developer/msu-tj-android.git](https://github.com/yusufjon-developer/msu-tj-android.git)
    ```
2.  **Setup Firebase:**
    * Create a project in the [Firebase Console](https://console.firebase.google.com/).
    * Download `google-services.json` and place it in the `app/` directory.
3.  Open the project in Android Studio (Ladybug or newer recommended).
4.  Sync Gradle and run on an Emulator/Device.

### 2. Go Backend
To run the backend tools:

1.  Clone the repository:
    ```bash
    git clone [https://github.com/yusufjon-developer/msu-tj-backend.git](https://github.com/yusufjon-developer/msu-tj-backend.git)
    ```
2.  Place your `serviceAccountKey.json` file in the project root.
3.  Run the server:
    ```bash
    go run cmd/app/main.go
    ```

## 📄 License

This project is distributed under the MIT License. See the [LICENSE](LICENSE) file for more details.
