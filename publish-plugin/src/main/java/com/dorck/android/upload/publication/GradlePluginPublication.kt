package com.dorck.android.upload.publication

import com.dorck.android.upload.config.JavadocJarOptions
import com.dorck.android.upload.extensions.publications
import com.dorck.android.upload.extensions.withJavadocJar
import com.dorck.android.upload.extensions.withSourcesJar
import com.dorck.android.upload.task.JavaDocJarTask.Companion.javadocJarTask
import com.dorck.android.upload.task.SourceJarTask.Companion.javaSourceJarTask
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

/**
 * To be used for `java-gradle-plugin` projects. Uses the default publication that gets created by that plugin.
 * Depending on the passed parameters for [javadocOptions] and [packSourceJar],
 * `-javadoc` and `-sources` jars will be added to the publication.
 *
 * Equivalent Gradle set up:
 * ```
 * publishing {
 *   publications {
 *     withType<MavenPublication>().configureEach {
 *       artifact sourceJar
 *     }
 *   }
 * }
 * ```
 * Refer at: [org.gradle.plugin.devel.plugins.MavenPluginPublishPlugin]
 */
data class GradlePluginPublication @JvmOverloads constructor(
    override val javadocOptions: JavadocJarOptions,
    override val packSourceJar: Boolean = false
) : PlatformPublication() {
    override fun setupPublication(project: Project, configure: MavenPublication.() -> Unit) {
        project.run {
            publications?.withType(MavenPublication::class.java)?.all {
                println("====> setupPublication of Gradle lib.")
                configure()
                if (name == "pluginMaven") {
                    println("====> start config artifact task of Gradle lib.")
                    withSourcesJar(javaSourceJarTask(packSourceJar))
                    withJavadocJar(javadocJarTask(javadocOptions))
                }
            }
        }
    }

}