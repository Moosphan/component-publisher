package com.dorck.android.upload.publication

import com.dorck.android.upload.config.JavadocJarOptions
import com.dorck.android.upload.extensions.defaultMavenPublication
import com.dorck.android.upload.extensions.publications
import com.dorck.android.upload.extensions.withJavadocJar
import com.dorck.android.upload.extensions.withSourcesJar
import com.dorck.android.upload.task.JavaDocJarTask.Companion.javadocJarTask
import com.dorck.android.upload.task.SourceJarTask.Companion.kotlinSourceJarTask
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

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

data class KotlinJsPublication @JvmOverloads constructor(
    override val javadocOptions: JavadocJarOptions = JavadocJarOptions.Empty(),
    override val packSourceJar: Boolean = true
) : PlatformPublication() {

    override fun setupPublication(project: Project, configure: MavenPublication.() -> Unit) {
        // Create publication, since Kotlin/JS doesn't provide one by default.
        // https://youtrack.jetbrains.com/issue/KT-41582
        println("====> setupPublication of kt-js lib.")
        project.run {
            publications?.create(defaultMavenPublication, MavenPublication::class.java) {
                configure()
                from(components.getByName("kotlin"))
                withSourcesJar(kotlinSourceJarTask(packSourceJar))
                withJavadocJar(javadocJarTask(javadocOptions))
            }
        }
    }
}