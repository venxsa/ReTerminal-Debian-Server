plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.rk.debianproot"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.rk.debianproot"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables.useSupportLibrary = true
    }

    val releaseKeystoreFile = file("/tmp/release.keystore")
    val hasReleaseKeystore = releaseKeystoreFile.exists()

    signingConfigs {
        val releaseKeystoreFile = file("/tmp/release.keystore")

        create("release") {
            if (hasReleaseKeystore) {
                storeFile = releaseKeystoreFile
            if (releaseKeystoreFile.exists()) {
                storeFile = releaseKeystoreFile
            val keystoreFromEnv = File("/tmp/release.keystore")
            if (keystoreFromEnv.exists()) {
                storeFile = keystoreFromEnv
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            } else {
                println("[signing] Release keystore not found; release will fall back to debug signing for CI artifacts")
                enableV1Signing = true
                enableV2Signing = true
                println("[signing] Release keystore not found, using unsigned release configuration")
            }
        }

        getByName("debug") {
            // Use the default debug keystore Gradle provides if no custom keystore exists.
            storeFile = file("${'$'}{rootDir}/debug.keystore")
            storeFile = file(layout.buildDirectory.dir("../debug.keystore"))
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false

            signingConfig = if (hasReleaseKeystore) signingConfigs.getByName("release") else signingConfigs.getByName("debug")

            val releaseKeystoreFile = file("/tmp/release.keystore")
            signingConfig = if (releaseKeystoreFile.exists()) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "app_name", "Debian Proot Console")
        }

        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            resValue("string", "app_name", "Debian Proot Console (Debug)")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources.excludes.add("META-INF/DEPENDENCIES")
    }

    packaging {
        resources.excludes.add("META-INF/DEPENDENCIES")
    }
}

tasks.register("printVersionName") {
    doLast { println(android.defaultConfig.versionName) }
tasks.register("printVersionName") {
    doLast { println(android.defaultConfig.versionName) }
}

tasks.register("printVersionCode") {
    doLast { println(android.defaultConfig.versionCode) }
}

tasks.register("printVersionCode") {
    doLast { println(android.defaultConfig.versionCode) }
}

dependencies {
    implementation(project(":core:terminal-view"))
    implementation(project(":core:terminal-emulator"))

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.material3)

    implementation(libs.okhttp)
    implementation(libs.kotlinx.coroutines)
    implementation("org.apache.commons:commons-compress:1.26.2")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
}
