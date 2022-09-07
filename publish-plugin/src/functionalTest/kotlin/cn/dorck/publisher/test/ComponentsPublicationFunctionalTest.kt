package cn.dorck.publisher.test

import cn.dorck.publisher.test.extensions.AgpVersionMapping
import cn.dorck.publisher.test.extensions.LibraryType
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
        val buildResult = publishLibraryToMavenLocal(LibraryType.Java, AgpVersionMapping.AGP_3_6_0.gradleVersion)
        assertPublishSucceed(buildResult)
    }

    @Test
    fun `publish Java component to maven local with AGP 4_0_0`() {
        val buildResult = publishLibraryToMavenLocal(LibraryType.Java, AgpVersionMapping.AGP_4_0_0.gradleVersion)
        assertPublishSucceed(buildResult)
    }

    @Test
    fun `publish Java component to maven local with AGP 4_2_0`() {
        val buildResult = publishLibraryToMavenLocal(LibraryType.Java, AgpVersionMapping.AGP_4_2_0.gradleVersion)
        assertPublishSucceed(buildResult)
    }

    @Test
    fun `publish Java component to maven local with AGP 7_1_0`() {
        val buildResult = publishLibraryToMavenLocal(LibraryType.Java, AgpVersionMapping.AGP_7_1_0.gradleVersion)
        assertPublishSucceed(buildResult)
    }

    @Test
    fun `publish Kotlin component to maven local with AGP 7_2_0`() {
        val buildResult = publishLibraryToMavenLocal(LibraryType.Kotlin, AgpVersionMapping.AGP_7_2_0.gradleVersion)
        assertPublishSucceed(buildResult)
    }

    @Test
    fun `publish Android component to maven local with AGP 7_2_0`() {
        val buildResult = publishLibraryToMavenLocal(LibraryType.Android, AgpVersionMapping.AGP_7_2_0.gradleVersion)
        assertPublishSucceed(buildResult)
    }

    private fun assertPublishSucceed(result: BuildResult) {
        assertTrue(result.tasks.none { it.outcome == TaskOutcome.FAILED })
        assertTrue(result.output.contains(SUCCEED_MSG))
    }

    private fun publishLibraryToMavenLocal(libraryType: LibraryType, gradleVersion: String): BuildResult {
        if (!this::settingsFile.isInitialized || !this::buildFile.isInitialized) {
            throw FileNotFoundException("Cannot find `build.gradle` and `settings.gradle` file.")
        }
        settingsFile.writeText("")
        buildFile.writeText(libraryType.buildTestContent)
        testProjectDir.run {
            generateSourceFile(this, libraryType)
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

    private fun generateSourceFile(parent: File, libraryType: LibraryType): File =
        when(libraryType) {
            LibraryType.Java -> File(
                parent,
                "src/main/java/cn/dorck/test/TestJavaClass.java"
            ).apply {
                createIfNotExist().writeText("""
                    public class TestJavaClass {
                        void doSomething() {
                            System.out.println("java");
                        }
                    }
                """.trimIndent())
            }
            LibraryType.Kotlin -> File(
                parent,
                "src/main/kotlin/cn/dorck/test/TestKotlinClass.kt"
            ).apply {
                createIfNotExist().writeText("""
                    class TestKotlinClass {
                        fun doSomething() {
                            println("kotlin ...")
                        }
                    }
                """.trimIndent())
            }

            LibraryType.Android -> File(
                parent,
                "src/main/kotlin/cn/dorck/test/SimpleFragment.kt"
            ).apply {
                createIfNotExist().writeText("""
                    package com.dorck.android.library.sample
    
                    import androidx.fragment.app.Fragment
                    
                    class SimpleFragment : Fragment() {
                    
                        fun getName() = "SimpleFragment"
                    }
                """.trimIndent())
            }

            else -> throw UnsupportedOperationException("Not supported yet")
        }

    companion object {
        private const val SUCCEED_MSG = "Your component published succeed!"
    }
}