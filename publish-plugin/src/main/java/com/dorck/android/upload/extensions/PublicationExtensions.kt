package com.dorck.android.upload.extensions

import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.util.GradleVersion


internal fun MavenPublication.withSourcesJar(artifactTask: TaskProvider<*>?) {
    artifactTask?.let {
        if (GradleVersion.current() >= GRADLE_6_6) {
            artifact(it)
        } else {
            artifact(it.get())
        }
    }
}

internal fun MavenPublication.withJavadocJar(artifactTask: TaskProvider<*>?) {
    artifactTask?.let {
        if (GradleVersion.current() >= GRADLE_6_6) {
            artifact(it)
        } else {
            artifact(it.get())
        }
    }
}

internal val GRADLE_6_6 = GradleVersion.version("6.6")