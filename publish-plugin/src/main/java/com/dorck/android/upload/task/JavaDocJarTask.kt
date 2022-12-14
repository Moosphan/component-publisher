package com.dorck.android.upload.task

import com.dorck.android.upload.config.JavadocJarOptions
import com.dorck.android.upload.extensions.formatCapitalize
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
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
            tasks.register("emptyJavadocJarFor${name.formatCapitalize()}", JavaDocJarTask::class.java)

        private fun Project.simpleJavadocJar(): TaskProvider<*> =
            tasks.register("simpleJavadocJarFor${name.formatCapitalize()}", JavaDocJarTask::class.java) {
                val task = tasks.named(JavaPlugin.JAVADOC_TASK_NAME)
                dependsOn(task)
                from(task)
            }

        private fun Project.dokkaJavadocJar(dokka: JavadocJarOptions.Dokka): TaskProvider<*> =
            tasks.register("dokkaJavadocJarFor${name.formatCapitalize()}", JavaDocJarTask::class.java) {
                val task = tasks.named(dokka.taskName)
                dependsOn(task)
                from(task)
            }
    }
}