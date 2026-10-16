import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
}

group = "com.zuno.driveit"
version = "1.0.0"

kotlin {
    jvmToolchain(17)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.google.api.client)
    implementation(libs.google.api.services.drive)
    implementation(libs.google.oauth.client.jetty)
    testImplementation(libs.junit)
}

compose.desktop {
    application {
        mainClass = "com.zuno.driveit.MainKt"

        jvmArgs += listOf(
            "--add-modules", "jdk.httpserver"
        )

        nativeDistributions {
            modules("jdk.httpserver")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "DriveIt"
            packageVersion = "1.0.0"
            description = "Compress and upload folders to Google Drive"
            vendor = "Zuno"

            macOS {
                // iconFile.set(project.file("icon.icns")) // TODO: Convert icon.svg to .icns
            }
            windows {
                // iconFile.set(project.file("icon.ico")) // TODO: Convert icon.svg to .ico
                menuGroup = "DriveIt"
                upgradeUuid = "E3B8C5D4-2F1A-4E6C-9B3D-7A8F2C1E4D5B"
            }
            linux {
                // iconFile.set(project.file("icon.png")) // TODO: Convert icon.svg to .png
            }
        }
    }
}