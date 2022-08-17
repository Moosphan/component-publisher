package com.dorck.android.upload

/**
 * Component upload config options extension.
 * Note: This class must be open with default constructor.
 * @author Dorck
 * @since 2022/08/13
 */
open class PublishOptionsExtension {
    // If empty, use project's group.
    var group: String = ""
    // If empty, use project's name.
    var artifactId: String = ""
    // If empty, use project's version.
    var version: String = ""
    // If empty, use `local.properties` options.
    var userName: String = ""
    var password: String = ""
    var releaseRepoUrl: String = ""
    var snapshotRepoUrl: String = ""
    // Description written in pom file.
    var description: String = ""
    // Whether to package the source code.
    var packSourceCode: Boolean = true
    // Will transitive dependencies be required.
    var transitiveDependency: Boolean = false

    override fun toString(): String {
        return "{\n" +
                "\tgroup: $group,\n" +
                "\tartifactId: $artifactId,\n" +
                "\tversion: $version,\n" +
                "\tuserName: $userName,\n" +
                "\tpassword: $password,\n" +
                "\treleaseRepoUrl: $releaseRepoUrl,\n" +
                "\tpackSourceCode: $packSourceCode,\n" +
                "\ttransitiveDependency: $transitiveDependency,\n" +
                "}"
    }
}
