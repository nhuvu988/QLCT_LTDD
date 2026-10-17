plugins {
    // Android tạo APK; Compose biên dịch UI; KSP sinh mã triển khai cho Room.
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

android {
    // namespace là package của lớp R sinh tự động; compileSdk là bộ API dùng khi biên dịch.
    namespace = "com.example.qlct"
    compileSdk = 37

    defaultConfig {
        // Có thể truyền -PqlctApplicationId để cài bản kiểm tra riêng, giữ dữ liệu của bản đang dùng.
        applicationId = providers.gradleProperty("qlctApplicationId").getOrElse("com.example.qlct")
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        // Biên dịch theo Java 11; desugaring hỗ trợ API java.time trên Android cũ từ minSdk 24.
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    // Bật Jetpack Compose để viết giao diện bằng hàm Kotlin thay cho XML layout.
    buildFeatures { compose = true }
}

dependencies {
    // BOM thống nhất phiên bản các thư viện Compose; Material 3 cung cấp thành phần giao diện.
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.compose.material3)
    debugImplementation(libs.compose.ui.tooling)
    // ViewModel giữ trạng thái; runtime-compose thu thập Flow theo lifecycle màn hình.
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.ktx)
    // Room ánh xạ entity/DAO thành SQLite; KSP xử lý annotation để sinh mã truy cập dữ liệu.
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    coreLibraryDesugaring(libs.desugar)
    // testImplementation chạy JVM; androidTestImplementation chạy trên thiết bị hoặc máy ảo Android.
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
}

// Xuất schema Room vào app/schemas để đối chiếu các phiên bản và kiểm tra migration.
ksp { arg("room.schemaLocation", "$projectDir/schemas") }
