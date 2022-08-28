package com.dorck.android.upload.publication

import com.dorck.android.upload.config.JavadocJarOptions
import com.dorck.android.upload.extensions.publications
import com.dorck.android.upload.extensions.withJavadocJar
import com.dorck.android.upload.task.JavaDocJarTask.Companion.javadocJarTask
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

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
data class KotlinMultiplatformPublication @JvmOverloads constructor(
    override val javadocOptions: JavadocJarOptions = JavadocJarOptions.Empty()
) : PlatformPublication() {
    // Automatically added by Kotlin MPP plugin.
    override val packSourceJar: Boolean
        get() = false

    override fun setupPublication(project: Project, configure: MavenPublication.() -> Unit) {
        project.run {
            publications?.withType(MavenPublication::class.java)?.all {
                configure()
                withJavadocJar(javadocJarTask(javadocOptions))
            }
        }
    }
}