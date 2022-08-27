import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
//    signing
    id("com.gradle.plugin-publish") version "1.0.0-rc-1"
}

// Load and configure secrets of publication.
ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.secretKeyRingFile"] = null
ext["gradle.publish.key"] = null
ext["gradle.publish.secret"] = null
ext["ossrh.username"] = null
ext["ossrh.password"] = null

// We can get secrets from local.properties or system env.
val localPropsFile = project.rootProject.file(com.android.SdkConstants.FN_LOCAL_PROPERTIES)
if (localPropsFile.exists()) {
    val properties = gradleLocalProperties(rootDir)
    if (properties.isNotEmpty()) {
        properties.onEach { (key, value) ->
            ext[key.toString()] = value
        }
    }
} else {
    ext["signing.keyId"] = System.getenv("GPG_KEY_ID")
    ext["signing.password"] = System.getenv("GPG_PASSWORD")
    ext["signing.secretKeyRingFile"] = System.getenv("GPG_SECRET_KEY_RING_FILE")
    ext["gradle.publish.key"] = System.getenv("GRADLE_PUBLISH_KEY")
    ext["gradle.publish.secret"] = System.getenv("GRADLE_PUBLISH_SECRET")
    ext["ossrh.username"] = System.getenv("OSSRH_USERNAME")
    ext["ossrh.password"] = System.getenv("OSSRH_PASSWORD")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
}

group = PluginInfo.group
version = PluginInfo.version

pluginBundle {
    website = "https://github.com/Moosphan/component-publisher"
    vcsUrl = "https://github.com/Moosphan/component-publisher.git"
    tags = listOf("component publication", "publish library", "maven-publish", "android-library", "kotlin-library")
}

gradlePlugin {
    plugins {
        create(PluginInfo.name) {
            // Note: We need to make here id as same as module name, or
            // it will publish two different plugins.
            id = PluginInfo.id
            implementationClass = PluginInfo.implementationClass
            displayName = PluginInfo.displayName
            description = PluginInfo.description
        }
    }
}

dependencies {
    // Use gradle api
    implementation(gradleApi())
    compileOnly("com.android.tools.build:gradle:4.0.0")
    compileOnly(kotlin("stdlib"))
    // Use dokka for java docs.
    compileOnly("org.jetbrains.dokka:dokka-gradle-plugin:1.6.10")
    // Use JUnit test framework for unit tests
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("mavenPub") {
                group = PluginInfo.group
                artifactId = PluginInfo.artifactId
                version = PluginInfo.version
                from(components["java"])

                pom {
                    name.set(PluginInfo.artifactId)
                    description.set(PluginInfo.description)
                    url.set(PluginInfo.url)

                    scm {
                        connection.set(PluginInfo.scmConnection)
                        developerConnection.set(PluginInfo.scmConnection)
                        url.set(PluginInfo.url)
                    }

                    licenses {
                        license {
                            name.set("The Apache Software License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            distribution.set("repo")
                        }
                    }

                    developers {
                        developer {
                            name.set(PluginInfo.developer)
                            email.set(PluginInfo.email)
                        }
                    }
                }
            }
        }
        repositories {
            maven {
                val isLocal = version.toString().endsWith("LOCAL")
                val repoUrl = if (version.toString().endsWith("SNAPSHOT")) {
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                } else if (isLocal) {
                    layout.buildDirectory.dir("repos/locals").get().asFile.path
                } else {
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                }
                url = uri(repoUrl)
                if (!isLocal) {
                    credentials {
                        username = this@afterEvaluate.ext["ossrh.username"].toString()
                        password = this@afterEvaluate.ext["ossrh.password"].toString()
                    }
                }
            }
        }
    }
}

// If apply plugin `com.gradle.plugin-publish`, no need apply `signing` plugin.
//signing {
//    sign(publishing.publications)
//}

object PluginInfo {
    const val id = "cn.dorck.component.publisher"
    const val name = "componentPublishPlugin"
    const val group = "cn.dorck"
    const val artifactId = "component-publisher"
    const val implementationClass = "com.dorck.android.upload.ComponentUploadPlugin"
    const val version = "1.0.0"
    const val displayName = "Upload library for Android"
    const val description = "Gradle plugin to publish library component quickly."
    const val url = "https://github.com/Moosphan/component-publisher.git"
    const val scmConnection = "scm:git@github.com:Moosphan/component-publisher.git"
    const val developer = "Dorck"
    const val email = "moosphon@gmail.com"
}