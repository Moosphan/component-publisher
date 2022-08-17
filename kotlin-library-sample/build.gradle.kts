plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("publish-plugin")
}

publishOptions {
    group = "com.dorck.kotlin"
    version = "1.0.0-LOCAL"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}