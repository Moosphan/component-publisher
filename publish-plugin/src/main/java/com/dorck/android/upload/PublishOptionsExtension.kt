package com.dorck.android.upload

import com.dorck.android.upload.ext.defaultPwd
import com.dorck.android.upload.ext.defaultReleaseRepositoryUrl
import com.dorck.android.upload.ext.defaultSnapshotRepositoryUrl
import com.dorck.android.upload.ext.defaultUserName

/**
 * Component upload config options extension.
 * Note: This class must be open with default constructor.
 * TODO: test for visibility of default privacy properties.
 * @author Dorck
 * @since 2022/08/13
 */
open class PublishOptionsExtension {
    var group: String = ""
    var artifactId: String = ""
    var version: String = ""
    var userName: String = ""
        get() {
            if (field.isEmpty()) {
                field = defaultUserName
            }
            return field
        }
    var password: String = ""
        get() {
            if (field.isEmpty()) {
                field = defaultPwd
            }
            return field
        }
    var releaseRepoUrl: String = ""
        get() {
            if (field.isEmpty()) {
                field = defaultReleaseRepositoryUrl
            }
            return field
        }

    var snapshotRepoUrl: String = ""
        get() {
            if (field.isEmpty()) {
                field = defaultSnapshotRepositoryUrl
            }
            return field
        }

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
