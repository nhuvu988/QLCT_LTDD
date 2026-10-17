// Kho tải các plugin Gradle (Android, Compose, KSP).
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Cho phép Gradle tìm/cấp JDK tương thích theo cấu hình toolchain.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Kho tải thư viện cho các module; không cho module tự thêm kho khác để cấu hình nhất quán.
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Tên dự án trong IDE và module Android duy nhất được đưa vào bản build.
rootProject.name = "QLCT"
include(":app")
