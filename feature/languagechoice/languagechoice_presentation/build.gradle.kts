@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

apply {
    from("$rootDir/plugins/app-versions.gradle")
    from("$rootDir/plugins/android-compose.gradle")
    from("$rootDir/plugins/android-library.gradle")
}

android {
    namespace = "com.teachmeprint.languagechoice_presentation"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":feature:languagechoice:languagechoice_domain"))
}