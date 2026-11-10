plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.xposedkit)
}

android {
    namespace = "cc.meteormc.hmapro"
    compileSdk {
        version = release(37) {
            minorApiLevel = 2
        }
    }

    defaultConfig {
        applicationId = "cc.meteormc.hmapro"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        val storeFile = providers.gradleProperty("android.storeFile").orNull ?: return@signingConfigs
        val storePassword = providers.gradleProperty("android.storePassword").orNull ?: return@signingConfigs
        val keyAlias = providers.gradleProperty("android.keyAlias").orNull ?: return@signingConfigs
        val keyPassword = providers.gradleProperty("android.keyPassword").orNull ?: return@signingConfigs

        create("signing") {
            this.storeFile = rootProject.file(storeFile)
            this.storePassword = storePassword
            this.keyAlias = keyAlias
            this.keyPassword = keyPassword
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("signing")
            optimization {
                enable = true
            }
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.androidx.ktx)
    implementation(libs.dexkit)

    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.ui)

    implementation(libs.miuix.ui)
    implementation(libs.miuix.preference)
    implementation(libs.miuix.icons)
    implementation(libs.miuix.blur)
    implementation(libs.miuix.nav)
}