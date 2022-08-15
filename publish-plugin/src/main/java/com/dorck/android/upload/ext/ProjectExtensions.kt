package com.dorck.android.upload.ext

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.dorck.android.upload.PublishOptionsExtension
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.kotlin.dsl.existing
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.dokka.gradle.DokkaTask

internal const val defaultSnapshotRepositoryUrl = "https://www.wanlinruo.com/nexus/repository/maven-snapshots/"
internal const val defaultReleaseRepositoryUrl = "https://www.wanlinruo.com/nexus/repository/maven-releases/"
internal const val defaultUserName = "uploader"
internal const val defaultPwd = "uploader"

val Project.mavenPublishExt: PublishingExtension?
    get() = extensions.findByType(PublishingExtension::class.java)

val Project.defaultGroupId: String
    get() = layout.projectDirectory.asFileTree.asPath ?: ""

val Project.defaultArtifactId: String
    get() = name

fun Project.isAndroidApp(): Boolean =
    moduleType == ModuleType.ANDROID_APPLICATION

fun Project.isAndroidLibrary(): Boolean =
    moduleType == ModuleType.ANDROID_LIBRARY

val Project.moduleType: ModuleType
    get() = if (androidAppComponent() != null) {
        ModuleType.ANDROID_APPLICATION
    } else if (androidLibraryComponent() != null) {
        ModuleType.ANDROID_LIBRARY
    } else if (javaLibraryComponent() != null) {
        ModuleType.JAVA_LIBRARY
    } else {
        ModuleType.OTHER
    }

/**
 * Set up custom artifacts to a publication based on platforms.
 * Reference:
 * - Android artifact: https://developer.android.google.cn/studio/build/maven-publish-plugin
 * - Gradle doc:
 *      - https://docs.gradle.org/current/userguide/publishing_customization.html
 *      - https://docs.gradle.org/current/dsl/org.gradle.api.publish.maven.MavenPublication.html
 */
fun Project.setupPlatformPublication(
    packSourceCode: Boolean,
    configure: MavenPublication.() -> Unit
) {
    val platformMavenPublication: PlatformPublication = when {
        plugins.hasPlugin("org.jetbrains.kotlin.multiplatform") ->
            KotlinMultiplatform(defaultJavaDocOptions())
        plugins.hasPlugin("com.android.library") ->
            if (plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
                AndroidLibraryWithMultiVariant(packSourceCode)
            } else AndroidLibraryWithSingleVariant(packSourceJar = packSourceCode)
        plugins.hasPlugin("java-gradle-plugin") ->
            GradlePlugin(defaultJavaDocOptions(), packSourceCode)
        plugins.hasPlugin("org.jetbrains.kotlin.jvm") ->
            KotlinJvm(defaultJavaDocOptions(), packSourceCode)
        plugins.hasPlugin("org.jetbrains.kotlin.js") ->
            KotlinJs(defaultJavaDocOptions(), packSourceCode)
        plugins.hasPlugin("java-library") || plugins.hasPlugin("java")->
            JavaLibrary(defaultJavaDocOptions(), packSourceCode)
        else ->
            NoSupportedPlatformPublication("No compatible plugin found in project $name for publishing.")
    }
    platformMavenPublication.setupPublication(this, configure)
}

fun Project.createCustomPublishingTask(onFinish: () -> Unit) {
    tasks.register("publishComponent") {
        group = "publisher"
        // TODO: 2022/08/14 => Confirm whether need depend on assemble task of android.
        if (isAndroidLibrary()) {
            dependsOn(tasks.named("assemble${getDefaultBuildType()}"))
        }
        // Finally execute maven publish task depends on custom task.
        val publishTaskName = "publish" + DEFAULT_MAVEN_PUBLICATION_NAME.formatCapitalize() +
                "PublicationTo" + DEFAULT_MAVEN_PUBLICATION_NAME.formatCapitalize() + "Repository"
        val realMavenPublishTask = tasks.named(publishTaskName)
        realMavenPublishTask.get()?.let {
            finalizedBy(realMavenPublishTask)
        }.also {
            doLast {
                onFinish()
            }
        }
    }
}

// Config info within maven `publishing` dsl.
fun Project.mavenPublishingDsl(action: Action<PublishingExtension>) {
    extensions.configure(PublishingExtension::class.java, action)
}

fun Project.androidAppComponent(): ApplicationAndroidComponentsExtension? =
    extensions.findByType(ApplicationAndroidComponentsExtension::class.java)

fun Project.androidLibraryComponent(): LibraryAndroidComponentsExtension? =
    extensions.findByType(LibraryAndroidComponentsExtension::class.java)

fun Project.javaLibraryComponent(): JavaPluginExtension? =
    extensions.findByType(JavaPluginExtension::class.java)

/**
 * Obtain app module(com.android.application) like this:
 * <code>
 * android {
 *  buildTypes {}
 *  defaultConfig {}
 * }
 * </code>
 */
fun Project.androidProject(): AppExtension? =
    extensions.findByType(AppExtension::class.java)

/**
 * Obtain library module(com.android.library).
 */
fun Project.libraryProject(): LibraryExtension? =
    extensions.findByType(LibraryExtension::class.java)

enum class ModuleType {
    ANDROID_APPLICATION,
    ANDROID_LIBRARY,
    JAVA_LIBRARY,
    OTHER
    // Others see here:
    // https://docs.gradle.org/current/userguide/build_init_plugin.html#supported_gradle_build_types
}

// Obtain default [JavadocJarOptions] by env.
private fun Project.defaultJavaDocOptions(): JavadocJarOptions {
    return if (plugins.hasPlugin("org.jetbrains.dokka") || plugins.hasPlugin("org.jetbrains.dokka-android")) {
        JavadocJarOptions.Dokka(findDokkaTask())
    } else {
        tasks.withType(Javadoc::class.java).configureEach {
            val options = options as StandardJavadocDocletOptions
            val javaVersion = javaVersion()
            if (javaVersion.isJava9Compatible) {
                options.addBooleanOption("html5", true)
            }
            if (javaVersion.isJava8Compatible) {
                options.addStringOption("Xdoclint:none", "-quiet")
            }
        }
        JavadocJarOptions.Javadoc()
    }
}

private fun Project.javaVersion(): JavaVersion {
    try {
        val extension = project.extensions.findByType(JavaPluginExtension::class.java)
        if (extension != null) {
            val toolchain = extension.toolchain
            val version = toolchain.languageVersion.get().asInt()
            return JavaVersion.toVersion(version)
        }
    } catch (t: Throwable) {
        // ignore failures and fallback to java version in which Gradle is running
    }
    return JavaVersion.current()
}

private fun Project.findDokkaTask(): String {
    val tasks = tasks.withType(DokkaTask::class.java)
    return if (tasks.size == 1) {
        tasks.first().name
    } else {
        tasks.findByName("dokkaHtml")?.name ?: "dokka"
    }
}

private fun Project.getDefaultBuildType(): String {
    val defaultBuildType = "release"
    if (isAndroidLibrary()) {
        val buildTypes = libraryProject()?.buildTypes
        if (buildTypes.isNullOrEmpty()) {
            return defaultBuildType
        }
        buildTypes.forEach {
            println("===> buildType: ${it.name}")
            if (it.name == "release") {
                return defaultBuildType
            }
        }
        return buildTypes.first().name
    }
    return defaultBuildType

}
