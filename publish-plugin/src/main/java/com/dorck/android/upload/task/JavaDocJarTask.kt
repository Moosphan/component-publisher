package com.dorck.android.upload.task

import com.dorck.android.upload.ext.JavadocJarOptions
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar

open class JavaDocJarTask : Jar() {
    init {
        archiveClassifier.set("javadoc")
    }

    internal companion object {
        internal fun Project.javadocJarTask(javadocOptions: JavadocJarOptions) : TaskProvider<*>? {
            return when(javadocOptions) {
                is JavadocJarOptions.None -> null
                is JavadocJarOptions.Empty -> emptyJavaDocJar()
                is JavadocJarOptions.Javadoc -> simpleJavadocJar()
                is JavadocJarOptions.Dokka -> dokkaJavadocJar(javadocOptions)
            }
        }

        private fun Project.emptyJavaDocJar(): TaskProvider<*> =
            tasks.register("emptyJavadocJar", JavaDocJarTask::class.java)

        private fun Project.simpleJavadocJar(): TaskProvider<*> =
            tasks.register("simpleJavadocJar", JavaDocJarTask::class.java) {
                val task = tasks.named("javadoc")
                dependsOn(task)
                from(task)
            }

        private fun Project.dokkaJavadocJar(dokka: JavadocJarOptions.Dokka): TaskProvider<*> =
            tasks.register("dokkaJavadocJar", JavaDocJarTask::class.java) {
                val task = tasks.named(dokka.taskName)
                dependsOn(task)
                from(task)
            }
    }
}