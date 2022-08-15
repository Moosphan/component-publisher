package com.dorck.android.upload

import com.dorck.android.upload.ext.*
import groovy.util.Node
import groovy.util.NodeList
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Project.DEFAULT_VERSION
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.maven.MavenPom
import java.net.URI

/**
 * Plugin to publish libraries quickly based on `maven-publish`.
 * @author Dorck
 * @since 2022/08/13
 * Reference doc: https://docs.gradle.org/current/userguide/publishing_maven.html
 */
class ComponentUploadPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val moduleType = target.moduleType
        println("===> start apply upload plugin with module type: $moduleType.")
        // Skip application module.
        if (target.isAndroidApp()) {
            return
        }
        // Check and make depends on `maven-publish`.
        if (!target.plugins.hasPlugin("maven-publish")) {
            target.plugins.apply("maven-publish")
        }
        // Obtain plugin extension.
        val publishOptionsExtension = target.extensions
            .create("publishOptions", PublishOptionsExtension::class.java)
        // Begin to config publish dsl.
        target.afterEvaluate {
            println("===> publishOptions config: $publishOptionsExtension")
            setupLibraryMavenPublishing(this, publishOptionsExtension)
        }
    }

    private fun setupLibraryMavenPublishing(project: Project, publishOptions: PublishOptionsExtension) {
        publishOptions.checkOrSupplementOptions(project)
        project.mavenPublishingDsl {
            println("===> start config maven publishing repos.")
            // Configure maven repositories.
            repositories.maven {
                val repoUri: URI = if (publishOptions.version.endsWith("-SNAPSHOT")) {
                    URI.create(publishOptions.snapshotRepoUrl)
                } else if (publishOptions.version.endsWith("-LOCAL")) {
                    project.repositories.mavenLocal().url
                } else {
                    URI.create(publishOptions.releaseRepoUrl)
                }
                url = repoUri
                credentials.username = publishOptions.userName
                credentials.password = publishOptions.password
            }
            println("===> start make maven publishing artifacts.")
            // Set up publication info and custom artifacts.
            project.setupPlatformPublication(publishOptions.packSourceCode) {
                println("====> start configure MavenPublication.")
                groupId = publishOptions.group
                artifactId = publishOptions.artifactId
                version = publishOptions.version
                // Write component info into pom file.
                configurePomXml(pom, publishOptions)
            }
            println("===> start create custom upload task.")
            // Configure custom task to publish component quickly.
            project.createCustomPublishingTask {
                printFinalPublication(publishOptions, repositories.named("maven", MavenArtifactRepository::class.java).get())
            }
        }
    }

    private fun configurePomXml(pom: MavenPom, publishOptions: PublishOptionsExtension) {
        // Set brief info, ignore scm, developer and license info configs etc.
        pom.name.set(publishOptions.artifactId)
        pom.description.set(publishOptions.description)
        // Configure dependencies relationship in pom.
        makeUpComponentDependencies(pom, publishOptions.transitiveDependency)
    }

    /**
     * Todo: 2022/08/14 => Need confirm that dependency is transitive.
     */
    private fun makeUpComponentDependencies(pom: MavenPom, transitiveDependency: Boolean) {
        if (!transitiveDependency) {
            pom.withXml {
                val rootNode = asNode()
                val dependencyNodes = rootNode.get("dependencies") as NodeList
                println("===> current dependencies: $dependencyNodes")
                if (dependencyNodes.isNotEmpty()) {
                    rootNode.remove(dependencyNodes.first() as Node)
                }
                println("===> after remove dependencies pom content: \n${asString()}")
            }
        }
    }

    private fun printFinalPublication(publishOptions: PublishOptionsExtension, mavenArtifactRepository: MavenArtifactRepository) {
        println("===== Component\$${publishOptions.artifactId} published succeed ======")
        println("===== Dependency url: implementation '${publishOptions.group}:${publishOptions.artifactId}:${publishOptions.version}'")
        println("===== You published it at: ${mavenArtifactRepository.url}")
    }

    private fun PublishOptionsExtension.checkOrSupplementOptions(project: Project) {
        if (group.isEmpty()) {
            group = project.defaultGroupId
            println("===> lost option of [publishOptions\$group], use [$group] by default.")
        }
        if (artifactId.isEmpty()) {
            artifactId = project.defaultArtifactId
            println("===> lost option of [publishOptions\$artifactId], use [$artifactId] by default.")
        }
        if (version.isEmpty()) {
            if (project.version.toString().isEmpty() || DEFAULT_VERSION == project.version.toString()) {
                //throw IllegalArgumentException("You must specified version in ${project.name} publishOptions.")
            }
            version = project.version.toString()
            println("===> lost option of [publishOptions\$version], use [$version] by default.")
        }
        println("===> real [publishOptions\$version] is [$version]")
    }
}