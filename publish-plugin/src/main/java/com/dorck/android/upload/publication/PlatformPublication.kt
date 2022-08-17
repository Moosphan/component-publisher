package com.dorck.android.upload.publication

import com.dorck.android.upload.config.JavadocJarOptions
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

/**
 * Abstract ability of maven publications in different platforms.
 */
abstract class PlatformPublication {
    abstract val javadocOptions: JavadocJarOptions
    abstract val packSourceJar: Boolean

    internal abstract fun setupPublication(project: Project, configure: MavenPublication.() -> Unit)
}