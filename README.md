# EB-5 Guide Android App

This repository contains the source code for the **EB-5 Guide** Android application. The app is built entirely with Kotlin 2.0 and Jetpack Compose and provides localized learning content about the U.S. EB-5 immigrant investor program in English, Chinese, Vietnamese, and Korean.

## Project Highlights

- **Multi-language experience** with live switching across UI strings, article content, quizzes, and project listings.
- **Onboarding language selection** that persists with Jetpack DataStore.
- **Five-tab navigation** (Home, EB-5 Base, Quizzes, Progress, Projects) powered by Navigation Compose.
- **JSON-backed content** for articles, quizzes, and project showcases stored under `app/src/main/assets/content/<lang>/`.
- **MVVM architecture** with state stored via Kotlin Flows and DataStore preferences.
- **Compose Material 3 UI** with per-language color themes and accessibility-friendly layouts.

## Requirements

- Android Studio Iguana (or newer) with JDK 17
- Android Gradle Plugin 8.6+
- Kotlin 2.0.21
- Minimum SDK 26, target/compile SDK 35

## Getting Started

```bash
./gradlew tasks       # verify Gradle wrapper
./gradlew assembleDebug
```

Launch the `MainActivity` to run the application. The first launch presents the onboarding language picker, and subsequent launches take the user directly to the main experience.

## Project Structure

```
app/
  src/
    main/
      assets/content/{en|zh|vi|ko}/    # localized JSON content
      java/com/eb5/app/                # Kotlin sources
      res/values(-lang)/               # localized strings and theming
```

### JSON Schema

- `eb5_terms.json`: array of articles with `id`, `title`, `category`, `subcategory`, `description`, `shortDescription`, `examples`, `dayNumber`, and `isCompleted` flags.
- `eb5_quizzes.json`: array of quiz topics keyed by `termId` with `questions[]` objects containing `question`, `options[]`, and `correctAnswerIndex`.
- `projects.json`: array of investment project cards including `minInvestmentUsd`, `teaStatus`, and `jobCreationModel` (Direct or RegionalCenter).

## Testing

Unit and UI tests are configured with JUnit4 and Compose UI testing libraries. Run the default unit tests with:

```bash
./gradlew test
```

UI tests can be executed with:

```bash
./gradlew connectedAndroidTest
```

## License

This project is provided for demonstration purposes as part of the EB-5 Guide specification.
