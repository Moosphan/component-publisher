plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("publish-plugin")
}

// FIXME: 2022/08/14 => Why cannot use `LOCAL` version?
publishOptions {
    group = "com.dorck.kotlin"
    version = "1.0.2"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}