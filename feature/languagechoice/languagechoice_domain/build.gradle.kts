plugins {
    id("lingshot.app.version.plugin")
    id("lingshot.android.quality.plugin")
}

android {
    namespace = "com.lingshot.languagechoice_domain"
}

dependencies {
    implementation(libs.bundles.coroutines)
}
