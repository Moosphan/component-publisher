package com.dorck.android.upload.publication

import com.dorck.android.upload.config.JavadocJarOptions
import com.dorck.android.upload.extensions.publications
import com.dorck.android.upload.extensions.withJavadocJar
import com.dorck.android.upload.extensions.withSourcesJar
import com.dorck.android.upload.task.JavaDocJarTask.Companion.javadocJarTask
import com.dorck.android.upload.task.SourceJarTask.Companion.javaSourceJarTask
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.get

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
data class KotlinLibraryPublication @JvmOverloads constructor(
    override val javadocOptions: JavadocJarOptions = JavadocJarOptions.Empty(),
    override val packSourceJar: Boolean = true
) : PlatformPublication() {

    override fun setupPublication(project: Project, configure: MavenPublication.() -> Unit) {
        println("====> setupPublication of kt jvm lib.")
        project.run {
            publications?.create("maven", MavenPublication::class.java) {
                configure()
                from(components["java"])
                println("====> configure sourceJar")
                withSourcesJar(javaSourceJarTask(packSourceJar))
                withJavadocJar(javadocJarTask(javadocOptions))
            }
        }
    }
}