package com.dorck.android.upload.ext

import com.dorck.android.upload.task.JavaDocJarTask.Companion.javadocJarTask
import com.dorck.android.upload.task.SourceJarTask.Companion.javaSourceJarTask
import com.dorck.android.upload.task.SourceJarTask.Companion.kotlinSourceJarTask
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider

// Note: Every maven publication task based on this default name.
internal const val DEFAULT_MAVEN_PUBLICATION_NAME = "maven"

/**
 * Abstract ability of maven publications in different platforms.
 */
sealed class PlatformPublication {
    abstract val javadocOptions: JavadocJarOptions
    abstract val packSourceJar: Boolean

    internal abstract fun setupPublication(project: Project, configure: MavenPublication.() -> Unit)
}

/**
 * To be used for `java` and `java-library` projects. Applying this creates a publication for the component called
 * `java`. Depending on the passed parameters for [javadocOptions] and [packSourceJar],
 * `-javadoc` and `-sources` jars will be added to the publication.
 *
 * Equivalent Gradle set up:
 * ```
 * publishing {
 *   publications {
 *     create<MavenPublication>("maven") {
 *       from(components["java"])
 *     }
 *   }
 * }
 *
 * java {
 *   withSourcesJar()
 *   withJavadocJar()
 * }
 * ```
 */
data class JavaLibrary @JvmOverloads constructor(
    override val javadocOptions: JavadocJarOptions,
    override val packSourceJar: Boolean = true
) : PlatformPublication() {

    override fun setupPublication(project: Project, configure: MavenPublication.() -> Unit) {
        println("====> setupPublication of Java lib.")
        project.mavenPublishExt?.publications?.create(DEFAULT_MAVEN_PUBLICATION_NAME, MavenPublication::class.java) {
            configure()
            from(project.components.getByName("java"))
            withSourcesJar(project.javaSourceJarTask(packSourceJar))
            withJavadocJar(project.javadocJarTask(javadocOptions))
        }
    }
}

/**
 * To be used for `java-gradle-plugin` projects. Uses the default publication that gets created by that plugin.
 * Depending on the passed parameters for [javadocOptions] and [packSourceJar],
 * `-javadoc` and `-sources` jars will be added to the publication.
 *
 * Equivalent Gradle set up:
 * ```
 * java {
 *   withSourcesJar()
 *   withJavadocJar()
 * }
 * ```
 * Refer at: [org.gradle.plugin.devel.plugins.MavenPluginPublishPlugin]
 */
data class GradlePlugin @JvmOverloads constructor(
    override val javadocOptions: JavadocJarOptions,
    override val packSourceJar: Boolean = false
) : PlatformPublication() {
    override fun setupPublication(project: Project, configure: MavenPublication.() -> Unit) {
        project.mavenPublishExt?.publications?.withType(MavenPublication::class.java)?.all {
            println("====> setupPublication of Gradle lib.")
            configure()
            if (name == "pluginMaven") {
                withSourcesJar(project.javaSourceJarTask(packSourceJar))
                withJavadocJar(project.javadocJarTask(javadocOptions))
            }
        }
    }

}

/**
 * To be used for `com.android.library` projects. Applying this creates a publication for the component of the given
 * `variant`. Depending on the passed parameters for [variantName], [packSourceJar] and [packJavadoc],
 * `-javadoc` and `-sources` jars will be added to the publication.
 *
 * Equivalent Gradle set up:
 * ```
 * android {
 *   publishing {
 *    singleVariant("variant") {
 *      withSourcesJar()
 *      withJavadocJar()
 *    }
 *   }
 * }
 *
 * afterEvaluate {
 *   publishing {
 *     publications {
 *       create<MavenPublication>("variant") {
 *         from(components["variant"])
 *       }
 *     }
 *   }
 * }
 *```
 */
data class AndroidLibraryWithSingleVariant(
    val variantName: String = "release",
    override val packSourceJar: Boolean = true,
    val packJavadoc: Boolean = true
) : PlatformPublication() {
    override val javadocOptions: JavadocJarOptions = JavadocJarOptions.None()

    override fun setupPublication(project: Project, configure: MavenPublication.() -> Unit) {
        println("====> setupPublication of Android lib.")
        project.libraryProject()?.publishing {
            singleVariant(variantName) {
                if (packSourceJar) {
                    withSourcesJar()
                }
                if (packJavadoc) {
                    withJavadocJar()
                }
            }
        }
        val component = project.components.findByName(variantName)
            ?: throw IllegalStateException("No variant#$variantName found!")
        project.mavenPublishExt?.publications?.create(DEFAULT_MAVEN_PUBLICATION_NAME, MavenPublication::class.java) {
            configure()
            from(component)
        }
    }
}

/**
 * To be used for `com.android.library` projects. Applying this creates a publication for the component of the given
 * variants. Depending on the passed parameters for [packSourceJar] and [packJavadoc], `-javadoc` and `-sources` jars will
 * be added to the publication.
 *
 * If the [includedBuildTypeValues] and [includedFlavorDimensionsAndValues] parameters are not provided or
 * empty all variants will be published. Otherwise only variants matching those filters will be included.
 *
 * Equivalent Gradle set up (AGP 7.1.1):
 * android {
 *   publishing {
 *    multipleVariants {
 *      allVariants() // or calls to includeBuildTypeValues and includeFlavorDimensionAndValues
 *      withSourcesJar()
 *      withJavadocJar()
 *    }
 *   }
 * }
 *
 * afterEvaluate {
 *   publishing {
 *     publications {
 *       create<MavenPublication>("default") {
 *         from(components["default"])
 *       }
 *     }
 *   }
 * }
 */
data class AndroidLibraryWithMultiVariant(
    override val packSourceJar: Boolean = true,
    val packJavadoc: Boolean = true,
    val includedBuildTypeValues: Set<String> = emptySet(),
    val includedFlavorDimensionsAndValues: Map<String, Set<String>> = emptyMap(),
) : PlatformPublication() {
    override val javadocOptions: JavadocJarOptions
        get() = JavadocJarOptions.None()

    override fun setupPublication(project: Project, configure: MavenPublication.() -> Unit) {
        println("====> setupPublication of Android lib.")
        project.libraryProject()?.publishing {
            multipleVariants {
                if (includedBuildTypeValues.isEmpty() && includedFlavorDimensionsAndValues.isEmpty()) {
                    allVariants()
                } else {
                    if (includedBuildTypeValues.isNotEmpty()) {
                        includeBuildTypeValues(*includedBuildTypeValues.toTypedArray())
                    }
                    if (includedFlavorDimensionsAndValues.isNotEmpty()) {
                        includedFlavorDimensionsAndValues.forEach { (dimension, flavors) ->
                            includeFlavorDimensionAndValues(dimension, *flavors.toTypedArray())
                        }
                    }
                }
                if (packSourceJar) {
                    withSourcesJar()
                }
                if (packJavadoc) {
                    withJavadocJar()
                }
            }
        }
        val component = project.components.findByName(DEFAULT_MAVEN_PUBLICATION_NAME)
            ?: throw IllegalStateException("No component#maven found!")
        project.mavenPublishExt?.publications?.create(DEFAULT_MAVEN_PUBLICATION_NAME, MavenPublication::class.java) {
            configure()
            from(component)
        }
    }

}

/**
 * To be used for `org.jetbrains.kotlin.multiplatform` projects. Uses the default publications that gets created by
 * that plugin, including the automatically created `-sources` jars. Depending on the passed parameters for [javadocOptions],
 * `-javadoc` will be added to the publications.
 *
 * Equivalent Gradle set up:
 * ```
 * // Nothing to configure setup is automatic.
 * ```
 *
 * This does not include javadoc jars because there are no APIs for that available.
 */
data class KotlinMultiplatform @JvmOverloads constructor(
    override val javadocOptions: JavadocJarOptions = JavadocJarOptions.Empty()
) : PlatformPublication() {
    // Automatically added by Kotlin MPP plugin.
    override val packSourceJar: Boolean
        get() = false

    override fun setupPublication(project: Project, configure: MavenPublication.() -> Unit) {
        println("====> setupPublication of kt multi lib.")
        project.mavenPublishExt?.publications?.withType(MavenPublication::class.java)?.all {
            configure()
            withJavadocJar(project.javadocJarTask(javadocOptions))
        }
    }
}

/**
 * To be used for `org.jetbrains.kotlin.jvm` projects. Applying this creates a publication for the component called
 * `kotlin`. Depending on the passed parameters for [javadocOptions] and [packSourceJar],
 * `-javadoc` and `-sources` jars will be added to the publication.
 *
 * Equivalent Gradle set up:
 * ```
 * publications {
 *   create<MavenPublication>("maven") {
 *     from(components["java"])
 *     artifact(project.tasks.named("kotlinSourcesJar"))
 *   }
 * }
 * ```
 * This does not include javadoc jars because there are no APIs for that available.
 */
data class KotlinJvm @JvmOverloads constructor(
    override val javadocOptions: JavadocJarOptions = JavadocJarOptions.Empty(),
    override val packSourceJar: Boolean = true
) : PlatformPublication() {

    override fun setupPublication(project: Project, configure: MavenPublication.() -> Unit) {
        println("====> setupPublication of kt jvm lib.")
        project.mavenPublishExt?.publications?.create(DEFAULT_MAVEN_PUBLICATION_NAME, MavenPublication::class.java) {
            configure()
            from(project.components.getByName("java"))
            println("====> configure sourceJar")
            withSourcesJar(project.javaSourceJarTask(packSourceJar))
            withJavadocJar(project.javadocJarTask(javadocOptions))
        }
    }
}

/**
 * To be used for `org.jetbrains.kotlin.js` projects. Applying this creates a publication for the component called
 * `kotlin`. Depending on the passed parameters for [javadocOptions] and [packSourceJar], `-javadoc` and `-sources` jars will be
 * added to the publication.
 *
 * Equivalent Gradle set up:
 * ```
 * publications {
 *   create<MavenPublication>("maven") {
 *     from(components["kotlin"])
 *     artifact(project.tasks.named("kotlinSourcesJar"))
 *   }
 * }
 * ```
 * This does not include javadoc jars because there are no APIs for that available.
 */

data class KotlinJs @JvmOverloads constructor(
    override val javadocOptions: JavadocJarOptions = JavadocJarOptions.Empty(),
    override val packSourceJar: Boolean = true
) : PlatformPublication() {

    override fun setupPublication(project: Project, configure: MavenPublication.() -> Unit) {
        // Create publication, since Kotlin/JS doesn't provide one by default.
        // https://youtrack.jetbrains.com/issue/KT-41582
        println("====> setupPublication of kt-js lib.")
        project.mavenPublishExt?.publications?.create(DEFAULT_MAVEN_PUBLICATION_NAME, MavenPublication::class.java) {
            configure()
            from(project.components.getByName("kotlin"))
            withSourcesJar(project.kotlinSourceJarTask(packSourceJar))
            withJavadocJar(project.javadocJarTask(javadocOptions))
        }
    }
}

data class NoSupportedPlatformPublication(val message: String = "") : PlatformPublication() {
    override val javadocOptions: JavadocJarOptions = JavadocJarOptions.None()
    override val packSourceJar: Boolean = false

    override fun setupPublication(project: Project, configure: MavenPublication.() -> Unit) {
        println("====> setupPublication of unknown lib.")
        throw UnsupportedOperationException(message)
    }

}

/**
 * Specifies how the javadoc jar should be created.
 */
sealed class JavadocJarOptions {
    /**
     * Do not create a javadoc jar. This option is not compatible with Maven Central.
     */
    class None : JavadocJarOptions() {
        override fun equals(other: Any?): Boolean = other is None
        override fun hashCode(): Int = this::class.hashCode()
    }

    /**
     * Creates an empty javadoc jar to satisfy maven central requirements.
     */
    class Empty : JavadocJarOptions() {
        override fun equals(other: Any?): Boolean = other is Empty
        override fun hashCode(): Int = this::class.hashCode()
    }

    /**
     * Creates a regular javadoc jar using Gradle's default `javadoc` task.
     */
    class Javadoc : JavadocJarOptions() {
        override fun equals(other: Any?): Boolean = other is Javadoc
        override fun hashCode(): Int = this::class.hashCode()
    }

    /**
     * Creates a javadoc jar using Dokka's output. The argument is the name of the dokka task that should be used
     * for that purpose.
     */
    data class Dokka(val taskName: String) : JavadocJarOptions()
}

private fun MavenPublication.withSourcesJar(artifactTask: TaskProvider<*>?) {
    artifactTask?.let {
        artifact(it)
    }
}

private fun MavenPublication.withJavadocJar(artifactTask: TaskProvider<*>?) {
    artifactTask?.let {
        artifact(it)
    }
}
