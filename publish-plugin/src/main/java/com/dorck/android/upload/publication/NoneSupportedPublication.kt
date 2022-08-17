package com.dorck.android.upload.publication

import com.dorck.android.upload.config.JavadocJarOptions
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

data class NoneSupportedPublication(
    val message: String
) : PlatformPublication() {
    override val javadocOptions: JavadocJarOptions
        get() = JavadocJarOptions.None()
    override val packSourceJar: Boolean
        get() = false

    override fun setupPublication(project: Project, configure: MavenPublication.() -> Unit) {
        throw UnsupportedOperationException(message)
    }
}