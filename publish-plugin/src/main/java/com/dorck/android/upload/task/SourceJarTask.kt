package com.dorck.android.upload.task

import com.dorck.android.upload.extensions.androidProject
import com.dorck.android.upload.extensions.formatCapitalize
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.the

open class SourceJarTask : Jar() {
    init {
        archiveClassifier.set("sources")
    }

    internal companion object {
        internal fun Project.javaSourceJarTask(packSource: Boolean): TaskProvider<*> =
            if (!packSource) {
                emptySourceJar()
            } else tasks.register("javaSourceJarFor${name.formatCapitalize()}", SourceJarTask::class.java) {
                dependsOn(JavaPlugin.CLASSES_TASK_NAME)
                val sourceSets = this@javaSourceJarTask.the<SourceSetContainer>()
                from(sourceSets["main"].allSource)
            }

        internal fun Project.kotlinSourceJarTask(packSource: Boolean): TaskProvider<*> =
            if (!packSource) {
                emptySourceJar()
            } else tasks.named("kotlinSourcesJar")

        internal fun Project.androidSourceJarTask(packSource: Boolean, variant: String): TaskProvider<*> =
            if (!packSource) {
                emptySourceJar()
            } else tasks.register("androidSourceJarFor$variant", SourceJarTask::class.java) {
                from(androidProject().sourceSets["main"].java.srcDirs)
            }

        private fun Project.emptySourceJar(): TaskProvider<*> =
            tasks.register("emptySourceJarFor${name.formatCapitalize()}", SourceJarTask::class.java)
    }
}