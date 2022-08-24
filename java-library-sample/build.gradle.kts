plugins {
    id("java-library")
    id("cn.dorck.component.publisher") version "1.0.0"
}

publishOptions {
    group = "com.dorck.java"
    version = "1.0.1-LOCAL"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}