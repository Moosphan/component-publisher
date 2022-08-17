package com.dorck.android.upload.config

/**
 * Specifies how the javadoc jar should be created.
 */
sealed class JavadocJarOptions {
    /**
     * Do not create a javadoc jar. This option is not compatible with Maven Central.
     */
    class None : JavadocJarOptions() {
        override fun equals(other: Any?): Boolean = other is None
        override fun hashCode(): Int = this::class.hashCode()
    }

    /**
     * Creates an empty javadoc jar to satisfy maven central requirements.
     */
    class Empty : JavadocJarOptions() {
        override fun equals(other: Any?): Boolean = other is Empty
        override fun hashCode(): Int = this::class.hashCode()
    }

    /**
     * Creates a regular javadoc jar using Gradle's default `javadoc` task.
     */
    class Javadoc : JavadocJarOptions() {
        override fun equals(other: Any?): Boolean = other is Javadoc
        override fun hashCode(): Int = this::class.hashCode()
    }

    /**
     * Creates a javadoc jar using Dokka's output. The argument is the name of the dokka task that should be used
     * for that purpose.
     */
    data class Dokka(val taskName: String) : JavadocJarOptions()
}