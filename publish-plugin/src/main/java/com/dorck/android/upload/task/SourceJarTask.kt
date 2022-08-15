package com.dorck.android.upload.task

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar

open class SourceJarTask : Jar() {
    init {
        archiveClassifier.set("sources")
    }

    internal companion object {
        internal fun Project.javaSourceJarTask(packSource: Boolean): TaskProvider<*> =
            if (!packSource) {
                emptySourceJar()
            } else tasks.register("javaSourceJar", SourceJarTask::class.java) {
                val javaPlugin = extensions.findByType(JavaPluginExtension::class.java)
                if (javaPlugin == null) {
                    println("====> cannot find JavaPluginExtension.")
                    return@register
                }
                from(javaPlugin.sourceSets.getByName("main").allSource)
            }

        internal fun Project.kotlinSourceJarTask(packSource: Boolean): TaskProvider<*> =
            if (!packSource) {
                emptySourceJar()
            } else tasks.named("kotlinSourcesJar")

        private fun Project.emptySourceJar(): TaskProvider<*> =
            tasks.register("emptySourceJar", SourceJarTask::class.java)
    }
}