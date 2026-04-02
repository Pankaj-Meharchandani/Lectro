# Lectro — Student Timetable & Study Companion

<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" alt="Lectro App Icon" width="120" height="120" />
</p>

<p align="center">
  A modern, single-activity Android app to manage your class schedule, track attendance, organize rich notes, and stay on top of assignments and exams — all in one place.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Language-Kotlin-orange" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4" />
  <img src="https://img.shields.io/badge/Architecture-Single%20Activity-blueviolet" />
  <img src="https://img.shields.io/badge/License-GNU-blue" />
</p>

---

## 📸 Screenshots

<p align="center">
  <img src="https://raw.githubusercontent.com/Pankaj-Meharchandani/Lectro/master/screenshots/Screenshot_20260325_203831.jpg" width="180" alt="Screen 1" />
  &nbsp;&nbsp;
  <img src="https://raw.githubusercontent.com/Pankaj-Meharchandani/Lectro/master/screenshots/Screenshot_20260326_124947.jpg" width="180" alt="Screen 2" />
  &nbsp;&nbsp;
  <img src="https://raw.githubusercontent.com/Pankaj-Meharchandani/Lectro/master/screenshots/Screenshot_20260326_124957.jpg" width="180" alt="Screen 3" />
  &nbsp;&nbsp;
  <img src="https://raw.githubusercontent.com/Pankaj-Meharchandani/Lectro/master/screenshots/Screenshot_20260326_125059.jpg" width="180" alt="Screen 4" />
</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/Pankaj-Meharchandani/Lectro/master/screenshots/Screenshot_20260326_125135.jpg" width="180" alt="Screen 5" />
  &nbsp;&nbsp;
  <img src="https://raw.githubusercontent.com/Pankaj-Meharchandani/Lectro/master/screenshots/Screenshot_20260326_125142.jpg" width="180" alt="Screen 6" />
  &nbsp;&nbsp;
  <img src="https://raw.githubusercontent.com/Pankaj-Meharchandani/Lectro/master/screenshots/Screenshot_20260326_125148.jpg" width="180" alt="Screen 7" />
  &nbsp;&nbsp;
  <img src="https://raw.githubusercontent.com/Pankaj-Meharchandani/Lectro/master/screenshots/Screenshot_20260326_125159.jpg" width="180" alt="Screen 8" />
</p>

---

## 🚀 Features

### 📅 Smart Timetable
- **Flexible View:** Toggle between 5-day (Mon–Fri) or 7-day (Mon–Sun) schedules.
- **Conflict Detection:** Intelligent conflict resolution when importing shared schedules.
- **Ongoing Class FAB:** Quick access to create a note for the class currently in progress.
- **Export to PDF:** Generate a clean, printable PDF of your entire weekly schedule.
- **Global Search:** Instant search across subjects, notes, assignments, and teachers.

### ✅ Attendance Tracking (Bunk Predictor)
- **Status tracking:** Mark classes as Present, Absent, or Cancelled.
- **Goal Management:** Set a custom goal (e.g., 75%) and get real-time "Safe/Unsafe" insights.
- **Predictive Logic:** Tells you exactly how many more classes you can skip or must attend to reach your goal.
- **History & Calendar:** Full historical log per subject with an interactive monthly calendar view.

### 📝 Pro Note Editor
- **WYSIWYG Markdown:** Real-time formatting for headings, bold, italic, underline, strikethrough, and code.
- **Intelligent Lists:** Checklists, bullet points, and numbered lists with auto-continuation.
- **Advanced Tools:** Inline image embedding, Find & Replace, and a Document Outline for quick navigation.
- **Statistics:** Real-time word count, character count, and reading time estimation.
- **PDF Export:** Share your notes as professional PDFs with preserved formatting.

### 📁 Subject & Material Management
- **Centralized Hub:** View all notes, materials, and attendance records for a specific subject.
- **File Attachments:** Upload and manage lecture slides, PDFs, and documents per subject.
- **Quick-Share:** Export individual subject schedules to share with classmates.

### 📋 Assignments & Exams
- **Deadline Tracking:** Organized tabs for Pending, Overdue, and Completed tasks.
- **Priority Reminders:** Automated notifications to ensure you never miss a deadline.
- **Inherited Theming:** Automatically color-coded based on the parent subject.

### 👤 Personal Details & Vault
- **Identity Profile:** Store your name, roll number, and student photo.
- **Secure File Storage:** Manage essential documents like admit cards or ID cards with custom labels.

### 📱 Home Screen Widgets (Glance)
- **Today's Schedule:** See your upcoming classes at a glance.
- **Deadlines Widget:** Keep track of approaching assignments and exams.
- **Attendance Widget:** Quick overview of your attendance standing across all subjects.

### 🔔 Smart Notifications
- **Class Reminders:** Triggered 15 minutes before every lecture.
- **Exam Alerts:** High-priority reminders 1 hour before the start.
- **Assignment Summary:** Morning digest at 8:00 AM for tasks due that day.
- **Weekly Report:** Every Sunday at 10:00 AM to review your attendance health.

---

## 🛠 Tech Stack

| Component | Technology |
|---|---|
| **Language** | Kotlin (100% UI), Java (Legacy Database/Utils) |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Navigation** | Compose Navigation (Single Activity) |
| **Widgets** | Jetpack Glance (Material 3) |
| **Persistence** | SQLite (via `SQLiteOpenHelper`) |
| **Image Loading** | Coil |
| **Reporting** | Android `PdfDocument` API |
| **Notifications** | AlarmManager + NotificationManager |

---

## 📂 Project Structure

```
app/src/main/java/com/example/timetable/
│
├── activities/          # MainActivity.kt (Entry point for Compose)
├── model/               # Data Models (Note, Subject, Exam, Homework, etc.)
│
├── ui/
│   ├── screens/         # Compose Screens (Main, Attendance, NoteInfo, ...)
│   ├── components/      # Reusable Compose Components (SubjectItem, NoteItem, ...)
│   ├── theme/           # Material 3 Color, Type, and Theme definitions
│   └── viewmodel/       # Shared State Management (MainViewModel)
│
├── utils/               # Logic (DbHelper, NotificationHelper, PdfGenerator, ...)
└── widget/              # Home Screen Widget Definitions (Glance)
```

---

## ⚙️ Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 30+ (Target SDK 35)
- Kotlin 2.0+

### Build & Run
1. Clone the repo: `git clone https://github.com/your-username/lectro.git`
2. Open in Android Studio.
3. Sync Gradle and Run on a device with API 30+.

> **Note:** Lectro is fully offline. All your data, notes, and files stay securely on your device.

---

## 🛡 Permissions

| Permission | Usage |
|---|---|
| `POST_NOTIFICATIONS` | Sending class, exam, and assignment reminders. |
| `SCHEDULE_EXACT_ALARM` | Ensuring reminders trigger precisely on time. |
| `RECEIVE_BOOT_COMPLETED` | Rescheduling alarms automatically after a phone restart. |
| `VIBRATE` | Haptic feedback for notifications. |

---

## 🙏 Acknowledgements
This project was initially inspired by [ulan17/TimeTable](https://github.com/ulan17/TimeTable). It provided the base ideas that Lectro was developed and expanded on — including the timetable, day fragments, and the SQLite database approach.
## 📄 License
This project is licensed under the **GNU General Public License v3.0**. See the [LICENSE](LICENSE) file for details.

---
