@file:Suppress("DSL_SCOPE_VIOLATION")

import com.google.gms.googleservices.GoogleServicesTask

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.dagger.hilt.android)
}

apply {
    from("$rootDir/plugins/app-versions.gradle")
    from("$rootDir/plugins/android-library.gradle")
    from("$rootDir/plugins/android-compose.gradle")
}

android {
    namespace = "com.teachmeprint.language"

    defaultConfig {
        applicationId = "com.teachmeprint.language"
    }
    gradle.projectsEvaluated {
        tasks.withType<GoogleServicesTask> {
            enabled = false
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:navigation"))
    implementation(project(":data:local"))
    implementation(project(":data:remote"))
    implementation(project(":domain"))
    implementation(project(":feature:home:home_presentation"))
    implementation(project(":feature:languagechoice:languagechoice_data"))
    implementation(project(":feature:languagechoice:languagechoice_domain"))
    implementation(project(":feature:languagechoice:languagechoice_presentation"))
    implementation(project(":feature:screencapture"))
    implementation(project(":feature:screenshot:screenshot_data"))
    implementation(project(":feature:screenshot:screenshot_domain"))
    implementation(project(":feature:screenshot:screenshot_presentation"))
    implementation(project(":feature:swipepermission:swipepermission_presentation"))

    implementation(libs.navigation.compose)
    implementation(libs.core.splash.screen)
    implementation(libs.hawk)

    implementation(libs.play.services.ads)
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)
}