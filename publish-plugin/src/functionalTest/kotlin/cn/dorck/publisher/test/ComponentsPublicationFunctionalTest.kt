package cn.dorck.publisher.test

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.FileNotFoundException
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Functional test cases for publishing components with different gradle versions.
 * Reference see: https://docs.gradle.org/current/userguide/test_kit.html
 * @author Dorck
 * @since 2022/09/07
 */
class ComponentsPublicationFunctionalTest {
    @TempDir
    public var testProjectDir: File? = null
    private lateinit var settingsFile: File
    private lateinit var buildFile: File

    @BeforeEach
    fun setup() {
        testProjectDir?.let {
            settingsFile = File(it, "settings.gradle.kts")
            buildFile = File(it, "build.gradle.kts")
        }
    }

    @Test
    fun `publish Java component to maven local by gradle 3_6`() {
        val buildResult = publishLibraryToMavenLocal("3.6.0")
        assertTrue(buildResult.tasks.none { it.outcome == TaskOutcome.FAILED })
    }

    private fun publishLibraryToMavenLocal(gradleVersion: String): BuildResult {
        if (!this::settingsFile.isInitialized || !this::buildFile.isInitialized) {
            throw FileNotFoundException("Cannot find `build.gradle` and `settings.gradle` file.")
        }
        settingsFile.writeText("")
        buildFile.writeText("""
            plugins {
                id("java-library")
                id("cn.dorck.component.publisher") version "1.0.4"
            }

            publishOptions {
                group = "com.dorck.java"
                version = "1.0.1-LOCAL"
            }
            
            repositories {
                mavenCentral()
                google()
            }
        """.trimIndent())
        testProjectDir?.run {
            File("src/main/java/cn/dorck/test/TestJavaClass.java").writeText("""
                public class TestJavaClass {
                    void doSomething() {
                        System.out.println("java");
                    }
                }
            """.trimIndent())
        }

        return GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withDebug(true)
            .withGradleVersion(gradleVersion)
            .forwardOutput()
            .build()
    }
}