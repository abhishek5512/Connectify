# 🚀 Connectify

**Connectify** is a modern Android application built to connect **Job Seekers** and **Employers** through a smart, swipe-based matching system.  
It provides job discovery, company insights, and real-time chat — all in one platform.

---

## 📱 Features

### 👩‍💼 For Employers
- Post and manage job openings  
- Add company logo, HR name, address, website, and email  
- View matched seekers who swiped right on their job posts  
- Chat directly with interested candidates  

### 👨‍🎓 For Job Seekers
- Swipe through jobs and apply with one click  
- View full company profile before applying  
- Maintain editable profile (photo, skills, qualification, age)  
- See matched jobs and chat with employers  

### 💬 Chat System
- Real-time messaging between seekers and employers  
- Firebase-backed chat data with read/write synchronization  

---

## 🛠️ Tech Stack

| Category | Tools / Frameworks |
|-----------|--------------------|
| **Language** | Java |
| **IDE** | Android Studio |
| **Database** | Firebase Firestore |
| **Authentication** | Firebase Auth |
| **Storage** | Firebase Storage |
| **UI Framework** | XML Layouts, RecyclerView, CardView |
| **Version Control** | Git & GitHub |

---

## 🗂️ Project Structure

Connectify/
│
├── app/
│ ├── java/connectify/
│ │ ├── activities/ (Login, Register, Chat, Dashboard, etc.)
│ │ ├── fragments/ (JobsFragment, ChatsFragment, ProfileFragment)
│ │ ├── adapters/ (JobAdapter, ChatAdapter, MatchAdapter)
│ │ ├── models/ (Job.java, User.java, Seeker.java, etc.)
│ │ └── utils/ (Helpers, Firebase services)
│ └── res/
│ ├── layout/ (All XML UI files)
│ ├── drawable/ (Icons, logos, backgrounds)
│ └── values/ (Strings, styles, colors)
│
├── build.gradle
├── settings.gradle
├── google-services.json (Not public)
└── README.md

yaml
Copy code

---

## ⚙️ Setup & Installation

1. **Clone this repository**

   git clone https://github.com/abhishek5512/Connectify.git

Open in Android Studio

Add your Firebase configuration

Place your google-services.json file in:

app/google-services.json
Sync Gradle and build the project.

Run on an emulator or Android device

📸 Screenshots (I will Add Later)
Login	
Job Swipe	
Chat	
Profile


🤝 Contributing
Contributions, feedback, and feature suggestions are always welcome!
Feel free to open an issue or submit a pull request.

🧑‍💻 Author
Abhishek Ishwar Gursali
📍 Chh. sambhajinagar, Maharashtra, India
💼 Developer of Connectify
📧 GitHub Profile

📜 License
This project is open-source and available under the MIT License.

⭐ If you like this project, give it a star on GitHub — it really helps!
