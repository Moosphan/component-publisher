package com.dorck.android.upload

import com.dorck.android.upload.config.Constants
import com.dorck.android.upload.extensions.*
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
        val moduleType = target.platformModule
        //println("===> start apply upload plugin with module type: $moduleType.")
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
            //println("===> publishOptions config: $publishOptionsExtension")
            setupLibraryMavenPublishing(this, publishOptionsExtension)
        }
    }

    private fun setupLibraryMavenPublishing(project: Project, publishOptions: PublishOptionsExtension) {
        publishOptions.checkOrSupplementOptions(project)
        //println("===> final publishOptions: $publishOptions")
        project.mavenPublishingDsl {
            // Configure maven repositories.
            repositories.maven {
                val isLocalPublish = publishOptions.version.endsWith("-LOCAL")
                val repoUri: URI = if (publishOptions.version.endsWith("-SNAPSHOT")) {
                    URI.create(publishOptions.snapshotRepoUrl)
                } else if (isLocalPublish) {
                    project.repositories.mavenLocal().url
                } else {
                    URI.create(publishOptions.releaseRepoUrl)
                }
                url = repoUri
                if (!isLocalPublish) {
                    credentials.username = publishOptions.userName
                    credentials.password = publishOptions.password
                }
            }
            // Set up publication info and custom artifacts.
            project.setupPlatformPublication(publishOptions.packSourceCode) {
                groupId = publishOptions.group
                artifactId = publishOptions.artifactId
                version = publishOptions.version
                // Write component info into pom file.
                configurePomXml(pom, publishOptions)
            }
            // Configure custom task to publish component quickly.
            project.createCustomPublishingTask {
                printFinalPublication(publishOptions, repositories.named(defaultMavenPublication, MavenArtifactRepository::class.java).get())
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

    private fun makeUpComponentDependencies(pom: MavenPom, transitiveDependency: Boolean) {
        if (!transitiveDependency) {
            pom.withXml {
                val rootNode = asNode()
                val dependencyNodes = rootNode.get("dependencies") as NodeList
                if (dependencyNodes.isNotEmpty()) {
                    rootNode.remove(dependencyNodes.first() as Node)
                }
            }
        }
    }

    private fun printFinalPublication(publishOptions: PublishOptionsExtension, mavenArtifactRepository: MavenArtifactRepository) {
        println("""
            ┌────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
            |                                           Your component published succeed!
            | Component artifactId: ${publishOptions.artifactId}
            | Component dependency url: implementation '${publishOptions.group}:${publishOptions.artifactId}:${publishOptions.version}'
            | Published at: ${mavenArtifactRepository.url}
            └────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
        """.trimIndent())
    }

    private fun PublishOptionsExtension.checkOrSupplementOptions(project: Project) {
        group = group.takeIfBlank { project.defaultGroupId }
        artifactId = artifactId.takeIfBlank { project.defaultArtifactId }
        if (version.isEmpty()) {
            if (project.version.toString().isEmpty() || DEFAULT_VERSION == project.version.toString()) {
                throw IllegalArgumentException("You must specified version in ${project.name} publishOptions.")
            }
            version = project.version.toString()
        }
        // Select publish options first, if parameter is empty, use `local.properties` instead.
        val defaultProperties = project.rootProject.loadPropertiesFile()
        defaultProperties?.run {
            userName = userName.takeIfBlank { getProperty(Constants.REPOSITORY_USERNAME_KEY) }
            password = password.takeIfBlank { getProperty(Constants.REPOSITORY_PASSWORD_KEY) }
            releaseRepoUrl = releaseRepoUrl.takeIfBlank { getProperty(Constants.REPOSITORY_RELEASE_URL) }
            snapshotRepoUrl = snapshotRepoUrl.takeIfBlank { getProperty(Constants.REPOSITORY_SNAPSHOT_URL) }
        }
        if (version.checkIfLocalVersion()) {
            return
        }
        // If not local version, you must specify the required repo info.
        check(userName.isNotEmpty() && password.isNotEmpty()
                && (releaseRepoUrl.isNotEmpty() || snapshotRepoUrl.isNotEmpty())) {
            "If not publish a LOCAL version, you must specify repo info, e.g, username|password|repoUrls."
        }
    }
}