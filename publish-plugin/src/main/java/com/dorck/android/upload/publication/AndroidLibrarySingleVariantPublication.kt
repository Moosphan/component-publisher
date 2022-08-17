package com.dorck.android.upload.publication

import com.dorck.android.upload.config.JavadocJarOptions
import com.dorck.android.upload.extensions.defaultMavenPublication
import com.dorck.android.upload.extensions.publications
import com.dorck.android.upload.extensions.withJavadocJar
import com.dorck.android.upload.extensions.withSourcesJar
import com.dorck.android.upload.task.JavaDocJarTask.Companion.javadocJarTask
import com.dorck.android.upload.task.SourceJarTask.Companion.androidSourceJarTask
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.repositories

/**
 * To be used for `com.android.library` projects. Applying this creates a publication for the component of the given
 * `variant`. Depending on the passed parameters for [variantName], [packSourceJar] and [packJavadoc],
 * `-javadoc` and `-sources` jars will be added to the publication.
 *
 * Equivalent Gradle set up:
 * ```
 * android {
 *   // Since AGP-7.1, we can still configure in the bottom `publishing` dsl.
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
data class AndroidLibrarySingleVariantPublication(
    val variantName: String = "release",
    override val packSourceJar: Boolean = true,
    val packJavadoc: Boolean = true
) : PlatformPublication() {
    override val javadocOptions: JavadocJarOptions = JavadocJarOptions.None()

    override fun setupPublication(project: Project, configure: MavenPublication.() -> Unit) {
        println("====> setupPublication of Android lib.")
        project.run {
            repositories {
                google()
            }
            publications?.create(defaultMavenPublication, MavenPublication::class.java) {
                configure()
                val component = components.findByName(variantName)
                    ?: throw IllegalStateException("No variant#$variantName found!")
                from(component)
                withSourcesJar(androidSourceJarTask(packSourceJar, variantName))
                withJavadocJar(javadocJarTask(javadocOptions))
            }
        }
    }
}