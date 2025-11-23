# Repository Guidelines

## Overall Rules
- 回答・説明・コメントはすべて 日本語で記述してください。
- コードは 省略せず完全な実装を出してください（ファイル単位で完結するように）。
- 実装の根拠・意図・判断理由を明確に説明してください。
- DRY / YAGNI / SOLID を尊重しつつ、Android / Kotlin の一般的な慣習を最優先してください。

## Project Structure & Modules
- `app/` : Android アプリ本体（Jetpack Compose、Hilt、Room、WebView を含む）。 
  - UIは`ui/`以下に配置する。 
  - Navigationは`ui/navigation/`以下にまとめる。 
  - Bookmark 保存処理は `data/`（Repository・Dao・Entity）に置く。 
- `gradle/`, `build.gradle.kts`, `settings.gradle.kts`:
  - 依存関係は `gradle/libs.versions.toml` に集約。 
  - Kotlin は `compilerOptions { jvmTarget.set(...) }` を使用し、kotlinOptions は使用禁止。

## Coding Style & Naming Conventions
- Kotlin
  - Kotlin official code style（4スペース）
  - package は lowercase（例: `jp.hotdrop.weeb.bookmark`） 
  - class / composable: UpperCamelCase（例: `BookmarkScreen`）
- Compose
  - 画面は`Screen`で命名。 
  - Previewは`Preview`。 
  - UI state は必ず hoist する（remember {} に過度に依存しない）。 
  - WebView は毎回新規生成せず、同じ instance を保持する（AI が誤りやすいので強調）。 
  - Modifier を必要な順序で組み立て、可読性を重視する。
- Navigation 
  - `Compose Navigation`を使用する。 
  - route は string literal を直接書かず、sealed class のオブジェクトで定義。 
  - ViewModel は画面ごとに Hilt で `@HiltViewModel` を使用し、`SavedStateHandle` を渡す。

## Dependency Injection (Hilt)
- Hilt + KSP を使用する。`kapt` は使用禁止。
- ViewModel は必ず `@HiltViewModel` + constructor injection。
- Repository も constructor injection。
- Hilt module は `@InstallIn(SingletonComponent::class)` を基本とする。

## Database (Room)
- Entity は data/entity/ に。
- Dao は data/dao/ に。
- Repository は interface + implementation の2層構造。
- Query は過剰に複雑化しない。必要なら @Query を適切に分割。

## WebView（このプロジェクト固有の重要ガイド）
- WebView は `AndroidView` を使って Compose 内に埋め込むが、 インスタンスは remember で 1 つだけ保持する（再生成禁止）。
- キャッシュは `LOAD_DEFAULT` のまま変更しない。
- Google ログインなどアカウント操作を自動化するコードは生成しない。
- `addJavascriptInterface` は使用禁止。

## Build & Gradle
- Kotlin 2.x の新 DSL（compilerOptions）を使用し、旧 API（kotlinOptions）は使わないこと。 
- `compileOptions { sourceCompatibility = VERSION_17; targetCompatibility = VERSION_17 }` を設定。 
- プラグインバージョンや依存は TOML（libs.versions.toml）で管理。
