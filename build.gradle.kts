// Khai báo phiên bản plugin dùng chung qua version catalog; app/build.gradle.kts mới áp dụng chúng.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
}
