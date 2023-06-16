@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.dagger.hilt.android)
}

apply {
    from("$rootDir/plugins/app-versions.gradle")
    from("$rootDir/plugins/android-compose.gradle")
    from("$rootDir/plugins/android-library.gradle")
}

android {
    namespace = "com.teachmeprint.screenshot_presentation"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":domain"))
    implementation(project(":feature:languagechoice:languagechoice_domain"))
    implementation(project(":feature:languagechoice:languagechoice_presentation"))
    implementation(project(":feature:screencapture"))
    implementation(project(":feature:screenshot:screenshot_domain"))

    implementation(libs.hilt.navigation.compose)
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)

    implementation(libs.image.cropper)
    implementation(libs.balloon.compose)
}