package com.dorck.android.upload.publication

import com.dorck.android.upload.config.JavadocJarOptions
import com.dorck.android.upload.extensions.defaultMavenPublication
import com.dorck.android.upload.extensions.publications
import com.dorck.android.upload.extensions.withJavadocJar
import com.dorck.android.upload.extensions.withSourcesJar
import com.dorck.android.upload.task.JavaDocJarTask.Companion.javadocJarTask
import com.dorck.android.upload.task.SourceJarTask.Companion.javaSourceJarTask
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.get

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
 *       artifact sourceJar
 *     }
 *   }
 * }
 * ```
 */
data class JavaLibraryPublication @JvmOverloads constructor(
    override val javadocOptions: JavadocJarOptions,
    override val packSourceJar: Boolean = true
) : PlatformPublication() {

    override fun setupPublication(project: Project, configure: MavenPublication.() -> Unit) {
        println("====> setupPublication of Java lib.")
        project.run {
            publications?.create(defaultMavenPublication, MavenPublication::class.java) {
                configure()
                from(components["java"])
                withSourcesJar(javaSourceJarTask(packSourceJar))
                withJavadocJar(javadocJarTask(javadocOptions))
            }
        }
    }
}