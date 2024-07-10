# PocketFin

PocketFin is an Android application designed to help users track their income and expenses, create and manage budgets, and analyze their financial status. Additionally, it integrates with Gemini to provide users with predictions and recommendations based on their spending habits.

## Features

- **User Registration and Login:**
  - User registration and authentication using Firebase Authentication.

- **Income and Expense Tracking:**
  - Interface for users to enter income and expenses categorized by type.
  - Data stored locally using SQLite or Room.

- **Budget Creation and Management:**
  - Users can create monthly budgets and track their spending against these budgets.

- **Reporting and Analysis:**
  - Generate income-expense reports.
  - Graphical representation of spending habits and budget status.

- **Predictions and Recommendations (Gemini Integration):**
  - Predict future expenses based on users' past spending data.
  - Provide savings recommendations based on spending habits.

## Technologies Used

- **Kotlin:** Primary programming language for the application.
- **Android SDK:** Development platform for Android applications.
- **Firebase Authentication:** User authentication and management.
- **SQLite or Room:** Local database management.
- **Gemini:** Data analysis and prediction engine.

## Installation

1. **Install Android Studio:**
   - Download and install [Android Studio](https://developer.android.com/studio).

2. **Clone the Project:**
   - Clone this GitHub repository or download it as a ZIP file.

3. **Firebase Setup:**
   - Create a new project in Firebase Console.
   - Enable Firebase Authentication and complete necessary configuration steps.

4. **Gemini Integration:**
   - Obtain Gemini API key and configure necessary connections.
   - Set up API calls for data retrieval and prediction features.

5. **SQLite or Room Database:**
   - Configure database operations within the project.

## Usage

1. Launch the application and login with Firebase Authentication or create a new account.
2. Enter your income and expenses categorized by type.
3. Create and monitor your monthly budget.
4. Analyze your financial status through reports and charts.
5. View predictions and recommendations provided by Gemini.

## Contributions

- Fork the repository on GitHub and submit pull requests for contributions.

## License

This project is licensed under the MIT License.
