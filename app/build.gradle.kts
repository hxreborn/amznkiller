import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.agp.app)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ktlint)
}

fun gitStdout(vararg args: String): String? {
    val stdout = ByteArrayOutputStream()
    val output =
        runCatching {
            exec {
                commandLine(args.toList())
                standardOutput = stdout
            }
            stdout.toString().trim()
        }.getOrNull()

    return output?.takeIf { it.isNotBlank() }
}

fun semverBase1000(versionName: String): Int {
    val semver = versionName.removePrefix("v").substringBefore("-")
    val parts = semver.split('.')
    val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
    return major * 1_000_000 + minor * 1_000 + patch
}

val gitCommitCount: Int by lazy {
    gitStdout("git", "rev-list", "--count", "HEAD")?.toIntOrNull() ?: 0
}

val gitDescribe: String by lazy {
    gitStdout("git", "describe", "--tags", "--always")?.removePrefix("v") ?: "0.0.0"
}

val gitSha: String by lazy {
    gitStdout("git", "rev-parse", "--short", "HEAD") ?: "unknown"
}

android {
    namespace = "eu.hxreborn.amznkiller"
    compileSdk = 36

    defaultConfig {
        applicationId = namespace
        minSdk = 31
        targetSdk = 36

        val baseVersionName =
            project.findProperty("version.name")?.toString()
                ?: gitDescribe

        versionName = baseVersionName
        versionCode = semverBase1000(baseVersionName) * 1000 + gitCommitCount
        buildConfigField("String", "GIT_SHA", "\"$gitSha\"")
    }

    androidResources {
        localeFilters += "en"
    }

    signingConfigs {
        create("release") {
            fun secret(name: String): String? =
                providers
                    .gradleProperty(name)
                    .orElse(providers.environmentVariable(name))
                    .orNull

            val storeFilePath = secret("RELEASE_STORE_FILE")
            if (!storeFilePath.isNullOrBlank()) {
                storeFile = file(storeFilePath)
                storePassword = secret("RELEASE_STORE_PASSWORD")
                keyAlias = secret("RELEASE_KEY_ALIAS")
                keyPassword = secret("RELEASE_KEY_PASSWORD")
                storeType = secret("RELEASE_STORE_TYPE") ?: "PKCS12"
            } else {
                logger.warn("RELEASE_STORE_FILE not found. Release signing is disabled.")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile != null }
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packaging {
        resources {
            pickFirsts += "META-INF/xposed/*"
        }
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = false
        disable.addAll(listOf("PrivateApi", "DiscouragedPrivateApi"))
        ignoreTestSources = true
    }
}

android.applicationVariants.all {
    outputs.forEach { output ->
        if (output is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
            output.outputFileName = "amznkiller-v$versionName-$name.apk"
        }
    }
}

kotlin {
    jvmToolchain(21)
}

ktlint {
    version.set("1.8.0")
    android.set(true)
    ignoreFailures.set(false)
}

dependencies {
    compileOnly(libs.libxposed.api)
    implementation(libs.libxposed.service)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.core.ktx)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.activity.compose)
    implementation(libs.splashscreen)
}

tasks.named("preBuild").configure {
    dependsOn("ktlintCheck")
}
