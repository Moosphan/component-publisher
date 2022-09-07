package cn.dorck.publisher.test.extensions

import java.io.File

enum class AgpVersionMapping(val gradleVersion: String) {
    AGP_3_6_0("5.6.4"),
    AGP_4_0_0("6.1.1"),
    AGP_4_1_0("6.5.1"),
    AGP_4_2_0("6.7.1"),
    AGP_7_0_0("7.0.1"),
    AGP_7_1_0("7.2"),
    AGP_7_2_0("7.3.3"),
}

enum class LibraryType(val buildTestContent: String) {
    Android(
        """
            plugins {
                id("com.android.library")
                id("org.jetbrains.kotlin.android")
                id("cn.dorck.component.publisher") version "1.0.4"
            }

            android {
                compileSdk = 32

                defaultConfig {
                    minSdk = 21
                    targetSdk = 32

                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    consumerProguardFiles("consumer-rules.pro")
                }

                buildTypes {
                    release {
                        isMinifyEnabled = false
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                    }
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_1_8
                    targetCompatibility = JavaVersion.VERSION_1_8
                }
                kotlinOptions {
                    jvmTarget = "1.8"
                }
            }

            publishOptions {
                group = "com.dorck.android"
                version = "0.1.0-LOCAL"
                artifactId = "android-sample-library"
            }

        """.trimIndent()
    ),
    Java(
        """
            plugins {
                id("java-library")
                id("cn.dorck.component.publisher") version "1.0.4"
            }

            publishOptions {
                group = "com.dorck.java"
                version = "1.0.1-LOCAL"
                artifactId = "java-sample-library"
            }
            
            repositories {
                mavenCentral()
                google()
            }
        """.trimIndent()
    ),
    Kotlin(
        """
            plugins {
                id("java-library")
                id("org.jetbrains.kotlin.jvm")
                id("cn.dorck.component.publisher") version "1.0.4"
            }

            publishOptions {
                group = "com.dorck.kotlin"
                version = "1.0.0-LOCAL"
                artifactId = "kotlin-sample-library"
            }

            java {
                sourceCompatibility = JavaVersion.VERSION_1_7
                targetCompatibility = JavaVersion.VERSION_1_7
            }
        """.trimIndent()
    ),
    Gradle(""),
    KotlinJs(""),
    Other("")
}

fun File.createIfNotExist(): File {
    if (!this.exists()) {
        this.parentFile?.mkdirs()
        this.createNewFile()
    }
    return this
}