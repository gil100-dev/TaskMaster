# TaskMaster - 5-Unit Android Project

**Author:** [Your Name]
**Course:** Planning & Programming Systems (5 Units)
**Grade:** 12

## 📱 Project Description
TaskMaster is a modern Android application designed to help users organize their daily lives efficiently. It features a robust task management system with support for subtasks, tagging, collaborative filtering, and date-based notifications.

The project demonstrates mastery of advanced Android concepts including **Reactive MVVM Architecture**, **Room Database Persistence**, and **Event-Driven UI**.

## 🏗️ Architecture Overview
The application follows the **Model-View-ViewModel (MVVM)** architectural pattern to ensure separation of concerns and testability.

*   **View (UI Layer):**
    *   XML Layouts with **ViewBinding** (No `findViewById`).
    *   Fragments/Activities handle UI logic only (`MainActivity`, `TaskDetailActivity`, `StatisticsActivity`).
*   **ViewModel (State Layer):**
    *   `MainViewModel`, `StatisticsViewModel`.
    *   Uses `LiveData` and `StateFlow` to expose reactive data streams to the UI.
    *   Survives configuration changes (rotation).
*   **Model (Data Layer):**
    *   **Repository:** `TaskRepository` acts as the Single Source of Truth.
    *   **Database:** **Room** (SQLite abstraction) with `Task`, `Subtask`, and `Tag` entities.
    *   **Optimization:** Custom SQL queries for Sorting and Filtering; Indices for performance.

## ✨ Key Features
1.  **Task Management:** CRUD operations for Tasks and Subtasks.
2.  **Advanced Sorting & Filtering:**
    *   Sort by *Due Date* or *Priority*.
    *   Filter by *Completion Status*.
3.  **Statistics Dashboard:** Visual breakdown of task completion rates and activity.
4.  **Notifications:** Exact alarm scheduling for task deadlines using `AlarmManager`.
5.  **Robustness:** Input validation, empty states, and crash-safe navigation.

## 🛠️ Tech Stack
*   **Language:** Kotlin
*   **Components:** Room, ViewModel, LiveData, Flow, Navigation, ViewBinding, AlarmManager.
*   **Build System:** Gradle (Kotlin DSL).

## 🚀 How to Run
1.  Open the project in **Android Studio** (Ladybug or newer recommended).
2.  Sync Gradle files to download dependencies.
3.  Select an Emulator (API 31+) or physical device.
4.  Run `app` configuration.

## 🎓 Defense Notes
This project satisfies all requirements for the Israeli Ministry of Education 5-unit exam:
*   [x] Multiple Activities & Intents
*   [x] Meaningful GUI Interactions
*   [x] Persistent Database (Room) with Relationships
*   [x] Complex Algorithms (Custom Sorting/Stats)
*   [x] Background Processing (Notifications)
