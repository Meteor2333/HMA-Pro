pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
//        maven {
//            url = uri("https://maven.pkg.github.com/compose-miuix-ui/miuix")
//            credentials {
//                username = providers.gradleProperty("gpr.user").orNull
//                password = providers.gradleProperty("gpr.key").orNull
//            }
//            mavenContent {
//                snapshotsOnly()
//            }
//        }
    }
}

rootProject.name = "HMA-Pro"
include(":app")
