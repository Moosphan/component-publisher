plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("cn.dorck.component.publisher") version "1.0.4"
}

publishOptions {
    group = "com.dorck.kotlin"
    version = "1.0.0-LOCAL"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}