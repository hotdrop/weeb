# Repository Guidelines

## Overall Rules
- Please answer in Japanese.
- Please provide a detailed explanation in Japanese.
- Please provide all the implementation code without omissions.
- Please clearly explain the justification and reason for the change, and the intention of the implementation.
- Please adhere to the basic principles of software development, such as the DRY principle, YAGNI, and SOLID principle. However, it is okay to prioritize the conventions of Android and Kotlin over the principles.
- Never use NavigationStack in SwiftUI!!!

## Project Structure & Module Organization
- `app/`: Android app (Jetpack Compose, Hilt, Room). Entry points, activities, themes, and navigation live under `.../ui/`.
- `gradle/`, `build.gradle.kts`, `settings.gradle.kts`: Centralized dependency versions via `gradle/libs.versions.toml`.

## Coding Style & Naming Conventions
- Kotlin: Official style (`kotlin.code.style=official`). Use 4‑space indentation.
- Packages: lowercase (`jp.hotdrop.weeb...`).
- Classes/Composables: UpperCamelCase (e.g., `StartViewModel`, `PointUseConfirmScreen`).
- Functions/variables: lowerCamelCase.
- Compose: Keep UI state hoisted; suffix screens with `Screen`; preview names end with `Preview`.
- Dependency Injection: Android uses Hilt with KSP; prefer constructor injection.
