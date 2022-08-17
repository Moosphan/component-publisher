package com.dorck.android.upload.publication

import com.dorck.android.upload.config.JavadocJarOptions
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

/**
 * To be used for `com.android.library` projects. Applying this creates a publication for the component of the given
 * variants. Depending on the passed parameters for [packSourceJar] and [packJavadoc], `-javadoc` and `-sources` jars will
 * be added to the publication.
 *
 * If the [includedBuildTypeValues] and [includedFlavorDimensionsAndValues] parameters are not provided or
 * empty all variants will be published. Otherwise only variants matching those filters will be included.
 *
 * Equivalent Gradle set up (AGP 7.1.1):
 * android {
 *   publishing {
 *    multipleVariants {
 *      allVariants() // or calls to includeBuildTypeValues and includeFlavorDimensionAndValues
 *      withSourcesJar()
 *      withJavadocJar()
 *    }
 *   }
 * }
 *
 * afterEvaluate {
 *   publishing {
 *     publications {
 *       create<MavenPublication>("default") {
 *         from(components["default"])
 *       }
 *     }
 *   }
 * }
 */
data class AndroidLibraryMultiVariantPublication(
    override val packSourceJar: Boolean = true,
    val packJavadoc: Boolean = true,
    val includedBuildTypeValues: Set<String> = emptySet(),
    val includedFlavorDimensionsAndValues: Map<String, Set<String>> = emptyMap(),
) : PlatformPublication() {
    override val javadocOptions: JavadocJarOptions
        get() = JavadocJarOptions.None()

    override fun setupPublication(project: Project, configure: MavenPublication.() -> Unit) {
        TODO("Only supported since AGP 7.1.1")
    }

}