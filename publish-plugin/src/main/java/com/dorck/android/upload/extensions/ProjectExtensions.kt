package com.dorck.android.upload.extensions

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.dorck.android.upload.config.JavadocJarOptions
import com.dorck.android.upload.publication.*
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.jetbrains.dokka.gradle.DokkaTask

// TODO: 2022/08/17 => 诸如repo地址并不需要用户频繁去声明，所以可以根据gradle.properties读取，优先采用extension中声明的
internal const val defaultSnapshotRepositoryUrl = "https://www.wanlinruo.com/nexus/repository/maven-snapshots/"
internal const val defaultReleaseRepositoryUrl = "https://www.wanlinruo.com/nexus/repository/maven-releases/"
internal const val defaultUserName = "uploader"
internal const val defaultPwd = "uploader"
private const val DEFAULT_MAVEN_PUBLICATION_NAME = "maven"

val Project.publications: PublicationContainer?
    get() = extensions.findByType(PublishingExtension::class.java)?.publications

val Project.defaultGroupId: String
    // TODO: split package name from path
    get() = group.toString().takeIf(String::isNotBlank)?.takeUnless {
        it == rootProject.name || it.startsWith("${rootProject.name}.")
    } ?: parent?.defaultGroupId /*?: layout.projectDirectory.asFileTree.asPath */ ?: throw GradleException("unspecified project group")

val Project.defaultArtifactId: String
    get() = name

fun Project.isAndroidApp(): Boolean =
    platformModule == PlatformModule.ANDROID_APPLICATION

fun Project.isAndroidLibrary(): Boolean =
    platformModule == PlatformModule.ANDROID_LIBRARY

val Project.platformModule: PlatformModule?
    get() = when {
        plugins.hasPlugin("com.android.application") ->
            PlatformModule.ANDROID_APPLICATION
        plugins.hasPlugin("com.android.library") ->
            PlatformModule.ANDROID_LIBRARY
        plugins.hasPlugin("java-library") || plugins.hasPlugin("java") ->
            PlatformModule.JAVA_LIBRARY
        plugins.hasPlugin("org.jetbrains.kotlin.jvm") ->
            PlatformModule.KOTLIN_LIBRARY
        plugins.hasPlugin("java-gradle-plugin") ->
            PlatformModule.GRADLE_PLUGIN
        plugins.hasPlugin("org.jetbrains.kotlin.js") ->
            PlatformModule.KOTLIN_JS_LIBRARY
        plugins.hasPlugin("org.jetbrains.kotlin.multiplatform") ->
            PlatformModule.KOTLIN_MULTI_PLATFORM
        else -> null
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
    val platformPublication: PlatformPublication = when(platformModule) {
        PlatformModule.JAVA_LIBRARY -> JavaLibraryPublication(defaultJavaDocOptions(),packSourceCode)
        PlatformModule.KOTLIN_LIBRARY -> KotlinLibraryPublication(defaultJavaDocOptions(), packSourceCode)
        PlatformModule.GRADLE_PLUGIN -> GradlePluginPublication(defaultJavaDocOptions(), packSourceCode)
        PlatformModule.ANDROID_LIBRARY -> AndroidLibrarySingleVariantPublication("release", false)
        PlatformModule.KOTLIN_JS_LIBRARY -> KotlinJsPublication(defaultJavaDocOptions(), packSourceCode)
        PlatformModule.KOTLIN_MULTI_PLATFORM -> KotlinMultiplatformPublication(defaultJavaDocOptions())
        else -> NoneSupportedPublication("Other platform libraries not supported yet.")
    }
    platformPublication.setupPublication(this, configure)
}

fun Project.createCustomPublishingTask(onFinish: () -> Unit) {
    tasks.register("publishComponent") {
        group = "publisher"
        // TODO: 2022/08/14 => Confirm whether need depend on assemble task of android.
        if (isAndroidLibrary()) {
            dependsOn(tasks.named("assemble${getDefaultBuildType().formatCapitalize()}"))
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
fun Project.androidProject(): AppExtension =
    extensions.getByType(AppExtension::class.java)

/**
 * Obtain library module(com.android.library).
 */
fun Project.libraryProject(): LibraryExtension? =
    extensions.findByType(LibraryExtension::class.java)

enum class PlatformModule {
    ANDROID_APPLICATION,
    ANDROID_LIBRARY,
    JAVA_LIBRARY,
    KOTLIN_LIBRARY,
    GRADLE_PLUGIN,
    KOTLIN_JS_LIBRARY,
    KOTLIN_MULTI_PLATFORM
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