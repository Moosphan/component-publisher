plugins {
    `java-gradle-plugin`
    // kotlin dsl extensions plugin
    `kotlin-dsl`
    `maven-publish`
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
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
                val repoDir = if (version.toString().endsWith("SNAPSHOT")) {
                    "repos/snapshots"
                } else if (version.toString().endsWith("LOCAL")) {
                    "repos/locals"
                } else {
                    "repos/releases"
                }
                url = uri(layout.buildDirectory.dir(repoDir))
            }
//            mavenLocal()
        }
    }
}

object PluginInfo {
    const val id = "publish-plugin"
    const val name = "componentPublishPlugin"
    const val group = "com.dorck.android.plugin"
    const val artifactId = "publish-plugin"
    const val implementationClass = "com.dorck.android.upload.ComponentUploadPlugin"
    const val version = "1.0.2-alpha"
    const val displayName = "Upload library for Android"
    const val description = "Gradle plugin to publish library component quickly."
    const val url = "https://github.com/Moosphan/component-publisher.git"
    const val scmConnection = "scm:git@github.com:Moosphan/component-publisher.git"
    const val developer = "Dorck"
    const val email = "moosphon@gmail.com"
}