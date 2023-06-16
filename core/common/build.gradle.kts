@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.dagger.hilt.android)
}

apply {
    from("$rootDir/plugins/app-versions.gradle")
    from("$rootDir/plugins/android-library.gradle")
    from("$rootDir/plugins/build-config-admob.gradle")
}

android {
    namespace = "com.teachmeprint.common"

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.retrofit)

    implementation(libs.play.services.ads)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)
}