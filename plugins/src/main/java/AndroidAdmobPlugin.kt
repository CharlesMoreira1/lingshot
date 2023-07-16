@file:Suppress("UNUSED")

import com.android.build.gradle.LibraryExtension
import extension.configureAndroidCompose
import helper.KeyHelper
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class AndroidAdmobPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            val extension = extensions.getByType<LibraryExtension>()

            extension.apply {
                configureAndroidCompose(extension)

                buildTypes {
                    release {
                        resValue("string", "ADMOB_APP_ID", KeyHelper.getValue("ADMOB_APP_ID_RELEASE"))
                        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", KeyHelper.getValue("ADMOB_INTERSTITIAL_ID_RELEASE"))
                    }
                    debug {
                        resValue("string", "ADMOB_APP_ID", KeyHelper.getValue("ADMOB_APP_ID_DEBUG"))
                        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", KeyHelper.getValue("ADMOB_INTERSTITIAL_ID_DEBUG"))
                    }
                }
            }
        }
    }
}
