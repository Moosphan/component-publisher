package cn.dorck.publisher.test

import cn.dorck.publisher.test.extensions.AgpVersionMapping
import cn.dorck.publisher.test.extensions.createIfNotExist
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
 * Gradle release histories refer to: https://gradle.org/releases/
 * AGP and Gradle version chart refer to: https://developer.android.com/studio/releases/gradle-plugin
 * @author Dorck
 * @since 2022/09/07
 */
class ComponentsPublicationFunctionalTest {
    @field:TempDir
    lateinit var testProjectDir: File
    private lateinit var settingsFile: File
    private lateinit var buildFile: File

    @BeforeEach
    fun setup() {
        testProjectDir.also {
            settingsFile = File(it, "settings.gradle.kts")
            buildFile = File(it, "build.gradle.kts")
        }
    }

    @Test
    fun `publish Java component to maven local with AGP 3_6`() {
        val buildResult = publishLibraryToMavenLocal(AgpVersionMapping.AGP_3_6_0.gradleVersion)
        assertPublishSucceed(buildResult)
    }

    @Test
    fun `publish Java component to maven local with AGP 4_0_0`() {
        val buildResult = publishLibraryToMavenLocal(AgpVersionMapping.AGP_4_0_0.gradleVersion)
        assertPublishSucceed(buildResult)
    }

    @Test
    fun `publish Java component to maven local with AGP 4_2_0`() {
        val buildResult = publishLibraryToMavenLocal(AgpVersionMapping.AGP_4_2_0.gradleVersion)
        assertPublishSucceed(buildResult)
    }

    @Test
    fun `publish Java component to maven local with AGP 7_1_0`() {
        val buildResult = publishLibraryToMavenLocal(AgpVersionMapping.AGP_7_1_0.gradleVersion)
        assertPublishSucceed(buildResult)
    }

    private fun assertPublishSucceed(result: BuildResult) {
        assertTrue(result.tasks.none { it.outcome == TaskOutcome.FAILED })
        assertTrue(result.output.contains(SUCCEED_MSG))
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
                artifactId = "java-sample-library"
            }
            
            repositories {
                mavenCentral()
                google()
            }
        """.trimIndent())
        testProjectDir.run {
            File(
                this,
                "src/main/java/cn/dorck/test/TestJavaClass.java"
            ).createIfNotExist()
            .writeText("""
                public class TestJavaClass {
                    void doSomething() {
                        System.out.println("java");
                    }
                }
            """.trimIndent())
        }
        println("==> The temp project dir: ${testProjectDir.path}")
        return GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withDebug(true)
            .withGradleVersion(gradleVersion)
            .withArguments("publishComponent", "-S")
            .forwardOutput()
            .build()
    }

    companion object {
        private const val SUCCEED_MSG = "Your component published succeed!"
    }
}