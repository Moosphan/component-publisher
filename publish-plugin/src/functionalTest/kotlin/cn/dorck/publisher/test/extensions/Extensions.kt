package cn.dorck.publisher.test.extensions

import java.io.File

enum class AgpVersionMapping(val gradleVersion: String) {
    AGP_3_6_0("5.6.4"),
    AGP_4_0_0("6.1.1"),
    AGP_4_1_0("6.5.1"),
    AGP_4_2_0("6.7.1"),
    AGP_7_0_0("7.0.1"),
    AGP_7_1_0("7.2"),
    AGP_7_2_0("7.3.3"),
}

fun File.createIfNotExist(): File {
    if (!this.exists()) {
        this.parentFile?.mkdirs()
        this.createNewFile()
    }
    return this
}