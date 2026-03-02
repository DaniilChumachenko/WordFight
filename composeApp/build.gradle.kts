import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.targets.native.tasks.PodBuildTask

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    cocoapods {
        summary = "Compose shared module"
        homepage = "https://example.com/wordfight"
        ios.deploymentTarget = "14.0"

        pod("Google-Mobile-Ads-SDK") {
            version = "11.10.0"
            moduleName = "GoogleMobileAds"
        }
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    val toolchainSwiftLib = System.getenv("DEVELOPER_DIR")?.let { "$it/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift" }
        ?: "/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift"
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        val platformDir = if (iosTarget.name.contains("Simulator", ignoreCase = true)) "iphonesimulator" else "iphoneos"
        val swiftCompatLibDir = "$toolchainSwiftLib/$platformDir"
        val swiftCompatLibs = listOf(
            "libswiftCompatibility50.a",
            "libswiftCompatibility51.a",
            "libswiftCompatibility56.a",
            "libswiftCompatibilityConcurrency.a",
            "libswiftCompatibilityDynamicReplacements.a",
            "libswiftCompatibilityPacks.a"
        ).map { "$swiftCompatLibDir/$it" }
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = false
            binaryOption("bundleId", "com.chvma.wordfight.composeApp")
        }

        // Apply the same linker setup to pod frameworks (composeApp.framework) as well.
        iosTarget.binaries.withType<Framework>().configureEach {
            if (baseName.equals("composeApp", ignoreCase = true)) {
                binaryOption("bundleId", "com.chvma.wordfight.composeApp")
            }
            linkerOpts("-L$swiftCompatLibDir")
            linkerOpts(*swiftCompatLibs.toTypedArray())
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.vosk.android)
            implementation("androidx.compose.material:material-icons-extended:1.6.8")
            implementation(libs.admob)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.chvma.wordfight"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.chvma.wordfight"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

tasks.withType<PodBuildTask>().configureEach {
    if (name.contains("Google-Mobile-Ads-SDK", ignoreCase = true)) {
        // Work around sporadic Xcode build system crashes while creating build description for Pods.
        xcodeBuildSettings.put("COMPILER_INDEX_STORE_ENABLE", "NO")
        xcodeBuildSettings.put("SWIFT_ENABLE_EXPLICIT_MODULES", "NO")
        xcodeBuildSettings.put("CLANG_ENABLE_EXPLICIT_MODULES", "NO")
        xcodeBuildSettings.put("ONLY_ACTIVE_ARCH", "YES")
    }
}
