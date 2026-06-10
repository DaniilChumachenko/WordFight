import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.targets.native.tasks.PodBuildTask
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
}

@Suppress("DEPRECATION")
kotlin {
    cocoapods {
        summary = "Compose shared module"
        homepage = "https://example.com/wordfight"
        version = "1.0.0"
        ios.deploymentTarget = "14.0"

        pod("Google-Mobile-Ads-SDK") {
            version = "11.10.0"
            moduleName = "GoogleMobileAds"
        }
        pod("FirebaseFirestore") {
            version = "11.10.0"
        }
        pod("FirebaseFirestoreInternal") {
            version = "11.10.0"
        }
    }

    @Suppress("DEPRECATION")
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
            implementation("com.alphacephei:vosk-android:0.3.75@aar")
            implementation("net.java.dev.jna:jna:5.18.1@aar")
            implementation(libs.androidx.appcompat)
            implementation("androidx.compose.material:material-icons-extended:1.6.8")
            implementation(libs.admob)
            implementation(libs.firebase.firestore.ktx)
            implementation(libs.kotlinx.coroutines.play.services)
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
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation("io.github.alexzhirkevich:compottie:2.0.2")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.chvma.pronounceWord"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    val localProperties = Properties().apply {
        val f = rootProject.file("local.properties")
        if (f.exists()) load(f.inputStream())
    }

    signingConfigs {
        create("release") {
            storeFile = file("release-keystore.jks")
            storePassword = localProperties.getProperty("signing.storePassword")
            keyAlias = localProperties.getProperty("signing.keyAlias")
            keyPassword = localProperties.getProperty("signing.keyPassword")
        }
    }

    defaultConfig {
        applicationId = "com.chvma.pronounceWord"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 3
        versionName = "1.0.2"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
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
    // Work around Xcode 26 Pod linker/debug-dylib issues for synthetic CocoaPods builds.
    xcodeBuildSettings.put("COMPILER_INDEX_STORE_ENABLE", "NO")
    xcodeBuildSettings.put("SWIFT_ENABLE_EXPLICIT_MODULES", "NO")
    xcodeBuildSettings.put("CLANG_ENABLE_EXPLICIT_MODULES", "NO")
    xcodeBuildSettings.put("ONLY_ACTIVE_ARCH", "YES")
    xcodeBuildSettings.put("ENABLE_DEBUG_DYLIB", "NO")
}
